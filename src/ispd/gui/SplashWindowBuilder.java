package ispd.gui;

import ispd.Main;

import javax.swing.ImageIcon;
import java.net.URL;
import java.util.MissingResourceException;

public class SplashWindowBuilder
{
    // TODO: Perhaps over-engineering?
    private static final String DEFAULT_IMAGE_PATH = "gui/imagens/Splash.gif";
    private static final String DEFAULT_TEXT = "Copyright (c) 2010 - 2014 GSPD.  All rights reserved."; // TODO: 2 spaces?
    private String imagePath = "";
    private String text = "";
    private boolean shouldBeVisible = false;

    public static SplashWindow visibleDefaultSplashWindow ()
    {
        return SplashWindowBuilder.aSplashWindow()
                .withImage(SplashWindowBuilder.DEFAULT_IMAGE_PATH)
                .withText()
                .visible()
                .build();
    }

    SplashWindow build ()
    {
        final var window = new SplashWindow(this.getImage());
        window.setText(this.text);

        if (this.shouldBeVisible)
        {
            window.setVisible(true);
        }

        return window;
    }

    private SplashWindowBuilder visible ()
    {
        this.shouldBeVisible = true;
        return this;
    }

    private SplashWindowBuilder withText ()
    {
        this.text = SplashWindowBuilder.DEFAULT_TEXT;
        return this;
    }

    SplashWindowBuilder withImage (final String imagePath)
    {
        this.imagePath = imagePath;
        return this;
    }

    static SplashWindowBuilder aSplashWindow ()
    {
        return new SplashWindowBuilder();
    }

    private ImageIcon getImage ()
    {
        final var imageUrl = Main.class.getResource(this.imagePath);

        if (null != imageUrl)
            return new ImageIcon(imageUrl);

        // TODO: Study MRE arguments
        throw new MissingResourceException(
                "Missing .gif for splash window",
                URL.class.getName(),
                this.imagePath
        );
    }
}
