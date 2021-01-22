/*
 * Main.java
 * Date created: May 4, 2019
 */

package com.grantranda.asciiart;

import com.grantranda.asciiart.ASCIIArt.Brightness;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main class to handle command-line arguments and run ASCIIArt.
 *
 * @author Grant Randa
 */
public class Main {

    public static final int DEFAULT_WIDTH = 464;
    public static final int DEFAULT_HEIGHT = 261;
    public static final Brightness DEFAULT_BRIGHTNESS = Brightness.AVERAGE;
    public static final boolean DEFAULT_INVERTED_BRIGHTNESS = false;
    public static final boolean DEFAULT_COLORED = true;

    /**
     * Processes command-line arguments and calls {@link ASCIIArt#render(String, int, int, Brightness, boolean, boolean)}.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("i")
                .desc("[REQUIRED] the pathname of an image")
                .longOpt("image")
                .required(true)
                .hasArg()
                .build()
        );
        options.addOption(Option.builder("w")
                .desc("the image width")
                .longOpt("width")
                .required(false)
                .hasArg()
                .build()
        );
        options.addOption(Option.builder("h")
                .desc("the image height")
                .longOpt("height")
                .required(false)
                .hasArg()
                .build()
        );
        options.addOption(Option.builder("bmm")
                .desc("use the min/max brightness mapping to map brightness levels to ASCII characters")
                .longOpt("brightnessMinMax")
                .required(false)
                .build()
        );
        options.addOption(Option.builder("bl")
                .desc("use the luminosity brightness mapping to map brightness levels to ASCII characters")
                .longOpt("brightnessLuminosity")
                .required(false)
                .build()
        );
        options.addOption(Option.builder("ib")
                .desc("invert brightness levels of the image")
                .longOpt("invertBrightness")
                .required(false)
                .build()
        );
        options.addOption(Option.builder("mc")
                .desc("disable coloring")
                .longOpt("monochrome")
                .required(false)
                .build()
        );

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        try (Scanner in = new Scanner(System.in)) {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("i")) {
                String pathname = line.getOptionValue("i");
                int width = DEFAULT_WIDTH;
                if (line.hasOption("w")) {
                    width = Integer.parseInt(line.getOptionValue("w"));
                }
                int height = DEFAULT_HEIGHT;
                if (line.hasOption("h")) {
                    height = Integer.parseInt(line.getOptionValue("h"));
                }
                Brightness brightnessMapping = DEFAULT_BRIGHTNESS;
                if (line.hasOption("bmm")) {
                    brightnessMapping = Brightness.MIN_MAX;
                    if (line.hasOption("bl")) {
                        System.out.println("--brightnessLuminosity is ignored because --brightnessMinMax is set");
                    }
                } else if (line.hasOption("bl")) {
                    brightnessMapping = Brightness.LUMINOSITY;
                }
                boolean invertedBrightness = DEFAULT_INVERTED_BRIGHTNESS;
                if (line.hasOption("ib")) {
                    invertedBrightness = true;
                }
                boolean colored = DEFAULT_COLORED;
                if (line.hasOption("mc")) {
                    colored = false;
                }

                System.out.println();
                ASCIIArt.render(pathname, width, height, brightnessMapping, invertedBrightness, colored);
                in.nextLine();
            } else {
                System.out.println("Image pathname is required.");
                formatter.printHelp("ascii-art", options);
            }
        } catch (ParseException e) {
            System.out.println("Error parsing command-line arguments.");
            formatter.printHelp("ascii-art", options);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Unable to locate image.");
            formatter.printHelp("ascii-art", options);
            System.exit(1);
        }
    }
}
