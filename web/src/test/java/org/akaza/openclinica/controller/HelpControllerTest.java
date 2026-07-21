package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.core.CoreResources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class HelpControllerTest {

    private HelpController helpController;
    private HttpServletResponse response;
    private Properties mockDataInfo;

    @Before
    public void setUp() {
        helpController = new HelpController();
        mockDataInfo = new Properties();
        mockDataInfo.setProperty("help.base.url", "https://docs.openclinica.com");
        ReflectionTestUtils.setField(CoreResources.class, "DATAINFO", mockDataInfo);
        response = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void testRedirectWithValidMappingAndMdExtension() throws IOException {
        helpController.resolveHelp("cookbook_index", response);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertEquals("https://docs.openclinica.com/docs/how-to/cookbook_index.html", urlCaptor.getValue());
    }

    @Test
    public void testRedirectWithValidMappingNoBaseUrlTrailingSlash() throws IOException {
        mockDataInfo.setProperty("help.base.url", "https://docs.openclinica.com");
        helpController.resolveHelp("cookbook_index", response);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertEquals("https://docs.openclinica.com/docs/how-to/cookbook_index.html", urlCaptor.getValue());
    }

    @Test
    public void testRedirectWithValidMappingWithBaseUrlTrailingSlash() throws IOException {
        mockDataInfo.setProperty("help.base.url", "https://docs.openclinica.com/");
        helpController.resolveHelp("cookbook_index", response);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertEquals("https://docs.openclinica.com/docs/how-to/cookbook_index.html", urlCaptor.getValue());
    }

    @Test
    public void testRedirectFallbackOriginalUrl() throws IOException {
        String originalUrl = "https://example.com/unmapped";
        helpController.resolveHelp(originalUrl, response);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertEquals(originalUrl, urlCaptor.getValue());
    }

    @Test
    public void testRedirectWithEmptyBaseUrl() throws IOException {
        mockDataInfo.setProperty("help.base.url", "");
        helpController.resolveHelp("cookbook_index", response);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertEquals("https://docs.openclinica.com/docs/how-to/cookbook_index.html", urlCaptor.getValue());
    }
}
