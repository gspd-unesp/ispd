package ispd.gui;

import ispd.Main;

import javax.swing.*;
import java.net.URL;
import java.util.MissingResourceException;

public class SplashWindowBuilder
{
    // TODO: Perhaps over-engineering?
    private static final String defaultImagePath = "gui/imagens/Splash.gif";
    private static final String defaultText = "Copyright (c) 2010 - 2014 GSPD.  All rights reserved."; // TODO: 2 spaces?
    private String imagePath = "";
    private String text = "";
    private boolean shouldBeVisible = false;

    public static SplashWindow visibleDefaultSplashWindow ()
    {
        return aSplashWindow()
                .withImage(defaultImagePath)
                .withText(defaultText)
                .visible()
                .build();
    }

    public static SplashWindowBuilder aSplashWindow ()
    {
        return new SplashWindowBuilder();
    }

    public SplashWindowBuilder withImage (String imagePath)
    {
        this.imagePath = imagePath;
        return this;
    }

    public SplashWindowBuilder withText (String text)
    {
        this.text = text;
        return this;
    }

    public SplashWindowBuilder visible ()
    {
        this.shouldBeVisible = true;
        return this;
    }

    public SplashWindow build ()
    {
        final var window = new SplashWindow(getImage());
        window.setText(this.text);

        if (this.shouldBeVisible)
        {
            window.setVisible(true);
        }

        return window;
    }

    private ImageIcon getImage ()
    {
        final var imageUrl = Main.class.getResource(this.imagePath);

        if (imageUrl == null)
        {
            // TODO: Study MRE arguments
            throw new MissingResourceException(
                    "Missing .gif for splash window",
                    URL.class.getName(),
                    this.imagePath
            );
        }

        return new ImageIcon(imageUrl);
    }
}
