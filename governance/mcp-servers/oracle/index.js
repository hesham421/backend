import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import oracledb from "oracledb";

oracledb.outFormat = oracledb.OUT_FORMAT_OBJECT;
oracledb.fetchAsString = [oracledb.CLOB, oracledb.NUMBER, oracledb.DATE];

const HOST = process.env.ORACLE_HOST ?? "localhost";
const PORT = Number(process.env.ORACLE_PORT ?? 1521);
const SERVICE = process.env.ORACLE_SERVICE ?? "FREEPDB1";

const config = {
  user: process.env.ORACLE_USER ?? "LOAN_SYS",
  password: process.env.ORACLE_PASSWORD ?? "",
  connectString: process.env.ORACLE_CONNECT_STRING ?? `${HOST}:${PORT}/${SERVICE}`,
  poolMin: 0,
  poolMax: 4,
  poolIncrement: 1,
};

// Oracle's own name for the current user's schema — every query below is scoped
// to it, so the server never reaches into another schema by accident.
const SCHEMA = (process.env.ORACLE_SCHEMA ?? config.user).toUpperCase();

// Read-only by default, matching the restricted postgres server. Set
// ORACLE_ALLOW_WRITE=true to permit INSERT/UPDATE/DELETE/DDL.
const ALLOW_WRITE = String(process.env.ORACLE_ALLOW_WRITE ?? "false") === "true";
const READ_ONLY_START = /^\s*(select|with)\b/i;

let poolPromise;
function getPool() {
  poolPromise ??= oracledb.createPool(config);
  return poolPromise;
}

async function run(sql, binds = {}) {
  const pool = await getPool();
  const conn = await pool.getConnection();
  try {
    const result = await conn.execute(sql, binds, { autoCommit: ALLOW_WRITE });
    return result;
  } finally {
    await conn.close();
  }
}

const server = new Server(
  { name: "erp-oracle-mcp", version: "1.0.0" },
  { capabilities: { tools: {} } },
);

const TOOLS = [
  {
    name: "query",
    description:
      `Run a SQL statement against the Oracle database (${SCHEMA} @ ${config.connectString}). ` +
      (ALLOW_WRITE
        ? "Supports SELECT, INSERT, UPDATE, DELETE and DDL."
        : "Read-only: only SELECT / WITH statements are accepted.") +
      " Returns the resulting rows (if any) and row count.",
    inputSchema: {
      type: "object",
      properties: {
        sql: {
          type: "string",
          description: "The SQL statement to execute (no trailing semicolon)",
        },
        binds: {
          type: "object",
          description:
            "Optional named bind values for :name placeholders, e.g. {\"id\": 42}",
        },
        maxRows: {
          type: "number",
          description: "Maximum rows to return (default 200)",
        },
      },
      required: ["sql"],
    },
  },
  {
    name: "list_tables",
    description: `List all tables in the ${SCHEMA} schema.`,
    inputSchema: { type: "object", properties: {} },
  },
  {
    name: "describe_table",
    description: "List columns, types and nullability for a given table.",
    inputSchema: {
      type: "object",
      properties: {
        table: { type: "string", description: "Table name (case-insensitive)" },
      },
      required: ["table"],
    },
  },
];

server.setRequestHandler(ListToolsRequestSchema, async () => ({ tools: TOOLS }));

const text = (value) => ({
  content: [{ type: "text", text: JSON.stringify(value, null, 2) }],
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    if (name === "query") {
      const sql = String(args.sql).replace(/;\s*$/, "");
      if (!ALLOW_WRITE && !READ_ONLY_START.test(sql)) {
        return {
          isError: true,
          content: [
            {
              type: "text",
              text:
                "Refused: this server is read-only. Set ORACLE_ALLOW_WRITE=true " +
                "to permit statements other than SELECT / WITH.",
            },
          ],
        };
      }
      const pool = await getPool();
      const conn = await pool.getConnection();
      try {
        const result = await conn.execute(sql, args.binds ?? {}, {
          autoCommit: ALLOW_WRITE,
          maxRows: Number(args.maxRows ?? 200),
        });
        return text({
          rowCount: result.rows ? result.rows.length : (result.rowsAffected ?? 0),
          rows: result.rows ?? [],
        });
      } finally {
        await conn.close();
      }
    }

    if (name === "list_tables") {
      const result = await run(
        `SELECT table_name FROM all_tables WHERE owner = :owner ORDER BY table_name`,
        { owner: SCHEMA },
      );
      return text(result.rows);
    }

    if (name === "describe_table") {
      const result = await run(
        `SELECT column_name, data_type, data_length, data_precision, data_scale,
                nullable, data_default
           FROM all_tab_columns
          WHERE owner = :owner AND table_name = UPPER(:table)
          ORDER BY column_id`,
        { owner: SCHEMA, table: args.table },
      );
      return text(result.rows);
    }

    return {
      isError: true,
      content: [{ type: "text", text: `Unknown tool: ${name}` }],
    };
  } catch (err) {
    return {
      isError: true,
      content: [{ type: "text", text: `Error: ${err.message}` }],
    };
  }
});

const transport = new StdioServerTransport();
await server.connect(transport);
