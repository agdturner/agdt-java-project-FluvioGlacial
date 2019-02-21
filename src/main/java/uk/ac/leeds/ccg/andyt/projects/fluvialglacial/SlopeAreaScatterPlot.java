/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.ccg.andyt.projects.fluvialglacial;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import uk.ac.leeds.ccg.andyt.data.Data_BiNumeric;
import uk.ac.leeds.ccg.andyt.chart.examples.Chart_Scatter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import uk.ac.leeds.ccg.andyt.math.Math_BigDecimal;
//import org.apache.commons.math3.fitting.PolynomialCurveFitter

/**
 *
 * @author geoagdt
 */
public class SlopeAreaScatterPlot extends Chart_Scatter {

    boolean isHump;
    public double maxy;
    public double xAtMaxy;
    public double maxy2;
    public double xAtMaxy2;

    //private final Object[] data;
    public SlopeAreaScatterPlot() {
        //data = new Object[5];
    }

    public SlopeAreaScatterPlot(
            int degree,
            Object[] data,
            ExecutorService executorService,
            File file,
            String format,
            String title,
            int dataWidth,
            int dataHeight,
            String xAxisLabel,
            String yAxisLabel,
            boolean drawOriginLinesOnPlot,
            int decimalPlacePrecisionForCalculations,
            int decimalPlacePrecisionForDisplay,
            RoundingMode aRoundingMode) {
        init(
                executorService,
                file,
                format,
                title,
                dataWidth,
                dataHeight,
                xAxisLabel,
                yAxisLabel,
                drawOriginLinesOnPlot,
                decimalPlacePrecisionForCalculations,
                decimalPlacePrecisionForDisplay,
                aRoundingMode);
        setStartAgeOfEndYearInterval(0);
        setData(data);
        PolynomialCurveFitter pcf;
        pcf = PolynomialCurveFitter.create(degree);
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        ArrayList<Data_BiNumeric> theData_BiNumeric;
        theData_BiNumeric = (ArrayList<Data_BiNumeric>) data[0];
        Iterator<Data_BiNumeric> ite;
        ite = theData_BiNumeric.iterator();
        Data_BiNumeric generic_XYNumericalData;
        while (ite.hasNext()) {
            generic_XYNumericalData = ite.next();
            obs.add(generic_XYNumericalData.x.doubleValue(),
                    generic_XYNumericalData.y.doubleValue());
        }
        double[] coeffs = pcf.fit(obs.toList());
        title += ", y = ";
        BigDecimal coeffBD;
        String coeffS;

        double a = 0.0d;
        double b = 0.0d;
        double c = 0.0d;
        for (int i = coeffs.length - 1; i > -1; i--) {
            System.out.println(coeffs[i]);
            coeffBD = Math_BigDecimal.roundToAndSetDecimalPlaces(
                    BigDecimal.valueOf(coeffs[i]),
                    decimalPlacePrecisionForDisplay,
                    aRoundingMode);
            coeffS = coeffBD.toPlainString();
            String s;
            s = getCoeff(coeffBD, coeffS);
            if (i > 1) {
                if (!s.isEmpty()) {
                    title += "(" + s + "*x^" + i + ")+";
                }

                if (i == 2) {
                    a = Double.valueOf(s);
                }

            } else if (i == 1) {
                if (!s.isEmpty()) {
                    title += "(" + s + "*x)";
                    b = Double.valueOf(s);
                } else {
                    b = 0.0d;
                }
            } else if (!s.isEmpty()) {
                title += s;
                c = Double.valueOf(s);
            } else {
                c = 0.0d;
            }
            //title += "" + coeffs[i] + ",";
        }
        //title = title.substring(0, (title.length() - 3));
        //title += ")";

        PolynomialFunction pf;
        pf = new PolynomialFunction(coeffs);
        double minx = minX.doubleValue();
        double maxx = maxX.doubleValue();
        double range = maxx - minx;
        int intervals = 100;
        double interval = range / (double) intervals;

        maxy = Double.NEGATIVE_INFINITY;

        double x;
        double y;
        bestfit = new ArrayList<Data_BiNumeric>();
        for (int i = 0; i < 100; i++) {
            x = minx + interval * i;
            y = pf.value(x);
            if (y > maxy) {
                maxy = y;
                xAtMaxy = x;
            }
            generic_XYNumericalData = new Data_BiNumeric(
                    BigDecimal.valueOf(x),
                    BigDecimal.valueOf(y));
            bestfit.add(generic_XYNumericalData);
        }

        maxy2 = Double.NEGATIVE_INFINITY;
        if (a < 0) {
            maxy2 = c - ((b * b) / (4.0d * a));
            xAtMaxy2 = (-1.0d * b) / (2.0d * a);
            isHump = true;
        } else {
            isHump = false;
        }

        double SRMSE = 0.0d;
        double deltay;
        ite = theData_BiNumeric.iterator();
        while (ite.hasNext()) {
            generic_XYNumericalData = ite.next();
            x = generic_XYNumericalData.x.doubleValue();
            y = generic_XYNumericalData.y.doubleValue();
            deltay = Math.sqrt(Math.pow(y - pf.value(x), 2));
            SRMSE += deltay;
        }
        double MRMSE = SRMSE / (double) theData_BiNumeric.size();
        title += ", MRMSE = " + (Math_BigDecimal.roundToAndSetDecimalPlaces(
                BigDecimal.valueOf(MRMSE),
                decimalPlacePrecisionForDisplay,
                aRoundingMode)).toPlainString();
        //setTitle(title);
    }

    ArrayList<Data_BiNumeric> bestfit;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    @Override
    public void drawData() {
        super.drawData();
        Iterator<Data_BiNumeric> ite = bestfit.iterator();
        Data_BiNumeric generic_XYNumericalData;
        Point2D aPoint2D = null;
        if (ite.hasNext()) {
            generic_XYNumericalData = ite.next();
            aPoint2D = coordinateToScreen(
                    generic_XYNumericalData.getX(),
                    generic_XYNumericalData.getY());
        }
        setPaint(Color.GREEN);
        Point2D bPoint2D;
        Line2D aLine2D;
        while (ite.hasNext()) {
            generic_XYNumericalData = ite.next();
            bPoint2D = coordinateToScreen(
                    generic_XYNumericalData.getX(),
                    generic_XYNumericalData.getY());
            aLine2D = new Line2D.Double(aPoint2D, bPoint2D);
            //draw(aPoint2D);
            draw(aLine2D);
            aPoint2D = bPoint2D;
        }

        // Draw intercept
        setPaint(Color.RED);
        aPoint2D = coordinateToScreen(
                BigDecimal.valueOf(xAtMaxy2),
                maxY);
//                   BigDecimal.valueOf(maxy));
        bPoint2D = coordinateToScreen(
                BigDecimal.valueOf(xAtMaxy2),
                minY);
        aLine2D = new Line2D.Double(aPoint2D, bPoint2D);
        draw(aLine2D);
    }

    private String getCoeff(BigDecimal coeffBD, String coeffS) {
        String result = "";
        if (coeffBD.compareTo(BigDecimal.ZERO) != 0) {
            if (coeffBD.compareTo(BigDecimal.ZERO) == 1) {
                result += coeffS;
            } else {
                result += "-" + coeffBD.abs().toPlainString();
            }
        }
        return result;
    }
}
