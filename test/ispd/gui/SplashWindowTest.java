package ispd.gui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(SplashWindowTest.TIMEOUT)
class SplashWindowTest
{
    public static final int TIMEOUT = 5;

    @Test
    void displaySplashWindow ()
    {
        SplashWindowBuilder.visibleDefaultSplashWindow();
    }

    @Test
    void buildSplashWindow ()
    {
        SplashWindowBuilder
                .aSplashWindow()
                .withImage("gui/imagens/Splash.gif")
                .build();
    }

    @AfterEach
    @SuppressWarnings("StatementWithEmptyBody")
    private void runForever ()
    {
        do
        {
        } while (true);
    }
}