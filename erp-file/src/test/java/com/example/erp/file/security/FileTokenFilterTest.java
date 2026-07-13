package com.example.erp.file.security;

import com.example.erp.common.i18n.LocalizationService;
import com.example.erp.common.web.OperationCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Covers only the routing decision (shouldNotFilter) — the token validation itself is covered
 * by {@link FileTokenServiceTest}.
 */
@ExtendWith(MockitoExtension.class)
class FileTokenFilterTest {

    private FileTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new FileTokenFilter(
            mock(FileTokenService.class), mock(OperationCode.class),
            mock(LocalizationService.class), new ObjectMapper());
    }

    @Test
    void shouldNotFilter_uploadPath_returnsFalse() throws Exception {
        assertThat(invokeShouldNotFilter("/upload/abc123", "POST")).isFalse();
    }

    @Test
    void shouldNotFilter_downloadPath_returnsFalse() throws Exception {
        assertThat(invokeShouldNotFilter("/download/abc123", "GET")).isFalse();
    }

    @Test
    void shouldNotFilter_deleteSingleSegmentRoot_returnsFalse() throws Exception {
        assertThat(invokeShouldNotFilter("/abc123XYZ", "DELETE")).isFalse();
    }

    @Test
    void shouldNotFilter_getOnSingleSegmentRoot_returnsTrue() throws Exception {
        // Same shape path but wrong method (e.g. swagger-ui.html) must NOT be gated.
        assertThat(invokeShouldNotFilter("/swagger-ui.html", "GET")).isTrue();
    }

    @Test
    void shouldNotFilter_deleteMultiSegmentApiPath_returnsTrue() throws Exception {
        assertThat(invokeShouldNotFilter("/api/v1/files/501", "DELETE")).isTrue();
    }

    @Test
    void shouldNotFilter_unrelatedPath_returnsTrue() throws Exception {
        assertThat(invokeShouldNotFilter("/api/v1/files/upload-token", "POST")).isTrue();
    }

    private boolean invokeShouldNotFilter(String uri, String method) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        lenient().when(request.getRequestURI()).thenReturn(uri);
        lenient().when(request.getMethod()).thenReturn(method);

        var m = FileTokenFilter.class.getDeclaredMethod("shouldNotFilter", HttpServletRequest.class);
        m.setAccessible(true);
        return (boolean) m.invoke(filter, request);
    }
}
