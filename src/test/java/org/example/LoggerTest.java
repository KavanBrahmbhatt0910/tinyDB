package org.example;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.*;

public class LoggerTest {
    @Mock
    SimpleDateFormat sdf;
    @InjectMocks
    Logger logger;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogGeneral() throws Exception {
        when(sdf.format(any(Date.class))).thenReturn("formatResponse");

        Logger.logGeneral("message");
    }

    @Test
    public void testLogQuery() throws Exception {
        when(sdf.format(any(Date.class))).thenReturn("formatResponse");

        Logger.logQuery("message");
    }

    @Test
    public void testLogEvent() throws Exception {
        when(sdf.format(any(Date.class))).thenReturn("formatResponse");

        Logger.logEvent("message");
    }
}

//Generated with love by TestMe :) Please raise issues & feature requests at: https://weirddev.com/forum#!/testme