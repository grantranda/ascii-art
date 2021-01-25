/*
 * ASCIIArt.java
 * Date created: May 4, 2019
 */

package com.grantranda.asciiart;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.Color.*;

/**
 * ASCIIArt provides the functionality for printing an image to the console,
 * with each pixel being represented as an ASCII character.
 *
 * @author Grant Randa
 */
public class ASCIIArt {

    public enum Brightness {
        AVERAGE, MIN_MAX, LUMINOSITY
    }

    public static final int CHAR_PADDING = 2;
    public static final String BRIGHTNESS_SCALE = "`^\",:;Il!i~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$"; // L == 65
    public static final Color[] BASIC_COLORS = {BLACK, BLUE, CYAN, GREEN, RED, WHITE, YELLOW, MAGENTA};

    static {
        AnsiConsole.systemInstall();
    }

    private ASCIIArt() {

    }

    /**
     * Returns a Color from {@link #BASIC_COLORS} that is most similar to a given Color object.
     *
     * @param color a Color object containing RGB values.
     * @return a Color from {@link #BASIC_COLORS} with RGB values that are closest to the RGB
     * values of the source color.
     */
    public static Color getBasicColor(Color color) {
        int rgbDistance = 0;
        int minDistance = 255 * 3;
        Color basic = BASIC_COLORS[0];

        for (Color basicColor : BASIC_COLORS) {
            rgbDistance = Math.abs(color.getRed() - basicColor.getRed())
                    + Math.abs(color.getGreen() - basicColor.getGreen())
                    + Math.abs(color.getBlue() - basicColor.getBlue());
            if (rgbDistance < minDistance) {
                minDistance = rgbDistance;
                basic = basicColor;
            }
        }
        return basic;
    }

    /**
     * Resizes and returns an image based on the given dimensions.
     *
     * @param image  the source image to be resized.
     * @param width  the new width.
     * @param height the new height.
     * @return a BufferedImage object containing the resized image.
     */
    public static BufferedImage getResizedImage(BufferedImage image, int width, int height) {
        Image temp = image.getScaledInstance(width, height, 4);
        BufferedImage resized = new BufferedImage(width, height, 2);
        Graphics2D gd2 = resized.createGraphics();
        gd2.drawImage(temp, 0, 0, null);
        gd2.dispose();
        return resized;
    }

    /**
     * Returns an array containing integers that represent RGB values for each pixel in the given image.
     *
     * @param image  the source image.
     * @param width  the image width.
     * @param height the image height.
     * @return an integer array the size of the image's area containing RGB values.
     */
    public static int[] getRGBArray(BufferedImage image, int width, int height) {
        int area = width * height;
        int[] rgbArray = new int[area];
        return image.getRGB(0, 0, width, height, rgbArray, 0, width);
    }

    /**
     * Returns a matrix containing the RGB values from the given array encapsulated in Color objects.
     *
     * @param rgbArray an array containing RGB values in the form of integers.
     * @param width    the matrix width.
     * @param height   the matrix height.
     * @return a matrix of Color objects containing the RGB values from the given array.
     */
    public static Color[][] getRGBMatrix(int[] rgbArray, int width, int height) {
        Color[][] rgbMatrix = new Color[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rgbMatrix[y][x] = new Color(rgbArray[y * width + x]);
            }
        }
        return rgbMatrix;
    }

    /**
     * Returns a matrix containing the brightness levels of each RGB value in the given array.
     *
     * @param rgbMatrix         a matrix containing RGB values encapsulated in Color objects.
     * @param brightnessMapping the brightness mapping used to calculate brightness values.
     * @return a matrix of integers representing brightness levels.
     */
    public static int[][] getBrightnessMatrix(Color[][] rgbMatrix, Brightness brightnessMapping) {
        int width = rgbMatrix[0].length;
        int height = rgbMatrix.length;
        int[][] brightnessMatrix = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = rgbMatrix[y][x].getRed();
                int g = rgbMatrix[y][x].getGreen();
                int b = rgbMatrix[y][x].getBlue();

                double rWeight = 0.21;
                double gWeight = 0.72;
                double bWeight = 0.07;

                switch (brightnessMapping) {
                    case AVERAGE:
                        int average = (r + g + b) / 3;
                        brightnessMatrix[y][x] = average;
                        break;
                    case LUMINOSITY:
                        int minMaxAverage = (Math.max(Math.max(r, g), b) + Math.min(Math.min(r, g), b)) / 2;
                        brightnessMatrix[y][x] = minMaxAverage;
                        break;
                    case MIN_MAX:
                        int weightedAverage = (int) (rWeight * r + gWeight * g + bWeight * b);
                        brightnessMatrix[y][x] = weightedAverage;
                }
            }
        }
        return brightnessMatrix;
    }

    /**
     * Returns a matrix containing the inverse brightness levels of a given matrix.
     *
     * @param brightnessMatrix a matrix containing brightness levels.
     * @return a matrix of integers representing brightness levels.
     */
    public static int[][] getInvertedBrightnessMatrix(int[][] brightnessMatrix) {
        int width = brightnessMatrix[0].length;
        int height = brightnessMatrix.length;
        int[][] invertedBrightnessMatrix = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                invertedBrightnessMatrix[y][x] = (255 - brightnessMatrix[y][x]);
            }
        }
        return invertedBrightnessMatrix;
    }

    /**
     * Returns a matrix of ASCII characters representing brightness levels.
     *
     * @param brightnessMatrix an integer matrix containing brightness levels.
     * @return a matrix of ASCII characters. The characters are chosen based on a {@link #BRIGHTNESS_SCALE} that
     * contains a list of characters ordered by how much screen space they fill.
     */
    public static char[][] getASCIIMatrix(int[][] brightnessMatrix) {
        int width = brightnessMatrix[0].length;
        int height = brightnessMatrix.length;
        char[][] asciiMatrix = new char[height][width];
        char[] brightnessScale = BRIGHTNESS_SCALE.toCharArray();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double brightnessPercent = brightnessMatrix[y][x] / 255.0;
                int scaleIndex = (int) (brightnessScale.length * brightnessPercent);
                if (scaleIndex >= brightnessScale.length) {
                    scaleIndex = brightnessScale.length - 1;
                }
                asciiMatrix[y][x] = brightnessScale[scaleIndex];
            }
        }
        return asciiMatrix;
    }

    /**
     * Returns a matrix of color-coded ASCII characters.
     *
     * @param asciiMatrix a matrix of ASCII characters.
     * @param rgbMatrix   a matrix of Color objects.
     * @return a matrix of color-coded ASCII characters.
     */
    public static String[][] getColoredASCIIMatrix(char[][] asciiMatrix, Color[][] rgbMatrix) {
        int width = asciiMatrix[0].length;
        int height = asciiMatrix.length;
        String[][] coloredAsciiMatrix = new String[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String colorCode = "";
                Color basicColor = getBasicColor(rgbMatrix[y][x]);

                if (BLACK.equals(basicColor)) {
                    colorCode = colorCode + "@|black ";
                } else if (BLUE.equals(basicColor)) {
                    colorCode = colorCode + "@|blue ";
                } else if (CYAN.equals(basicColor)) {
                    colorCode = colorCode + "@|cyan ";
                } else if (GREEN.equals(basicColor)) {
                    colorCode = colorCode + "@|green ";
                } else if (RED.equals(basicColor)) {
                    colorCode = colorCode + "@|red ";
                } else if (WHITE.equals(basicColor)) {
                    colorCode = colorCode + "@|white ";
                } else if (YELLOW.equals(basicColor)) {
                    colorCode = colorCode + "@|yellow ";
                } else {
                    colorCode = colorCode + "@|magenta ";
                }
                colorCode = colorCode + asciiMatrix[y][x] + "|@";
                coloredAsciiMatrix[y][x] = colorCode;
            }
        }
        return coloredAsciiMatrix;
    }

    /**
     * Prints an image at the given path to the console, with each pixel represented as an ASCII character.
     *
     * @param pathname           the pathname of an image.
     * @param width              the image width.
     * @param height             the image height.
     * @param brightnessMapping  the brightness mapping used to map brightness levels to ASCII characters.
     * @param brightnessInverted if true, the brightness levels of the image will be inverted.
     * @param colored            if true, the printed ASCII characters will be colored.
     */
    public static void render(String pathname, int width, int height, Brightness brightnessMapping,
                              boolean brightnessInverted, boolean colored) throws IOException {

        BufferedImage image = ImageIO.read(new File(pathname));
        image = getResizedImage(image, width, height);

        int[] rgbArray = getRGBArray(image, width, height);
        Color[][] rgbMatrix = getRGBMatrix(rgbArray, width, height);
        int[][] brightnessMatrix = getBrightnessMatrix(rgbMatrix, brightnessMapping);

        if (brightnessInverted) {
            brightnessMatrix = getInvertedBrightnessMatrix(brightnessMatrix);
        }

        char[][] asciiMatrix = getASCIIMatrix(brightnessMatrix);

        String[][] coloredAsciiMatrix = null;
        if (colored) {
            coloredAsciiMatrix = getColoredASCIIMatrix(asciiMatrix, rgbMatrix);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int i = 0; i < CHAR_PADDING; i++) {
                    System.out.print(Ansi.ansi().render((colored ? coloredAsciiMatrix[y][x] : asciiMatrix[y][x]) + ""));
                }
            }
            System.out.println();
        }
    }
}
