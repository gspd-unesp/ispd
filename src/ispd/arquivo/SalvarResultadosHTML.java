package ispd.arquivo;

import ispd.gui.MainWindow;
import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasGlobais;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

/**
 * Stores simulation results and exports them to a html file
 */
public class SalvarResultadosHTML {
    private static final double EFFICIENCY_GOOD = 70.0;
    private static final double EFFICIENCY_BAD = 40.0;
    private String globalMetrics = null;
    private String tasks = null;
    private String table = null;
    private String chartsText = null;
    private BufferedImage[] chartImages = null;

    /**
     * Generates a table with the results of each service center
     *
     * @param table Table with the following columns: <b>Label, Owner,
     *              Processing performed, Communication performed</b>
     */
    public void setTabela(final Object[][] table) {
        this.table = """
                <table align="center" border="1" cellpadding="1" cellspacing="1" style="width: 80%%;">
                            <thead>
                                <tr>
                                    <th scope="col">
                                        <span style="color:#800000;">Label</span></th>
                                    <th scope="col">
                                        <span style="color:#800000;">Owner</span></th>
                                    <th scope="col">
                                        <span style="color:#800000;">Processing performed</span></th>
                                    <th scope="col">
                                        <span style="color:#800000;">Communication&nbsp;performed</span></th>
                                </tr>
                            </thead>
                            <tbody>
                            %s
                            </tbody>
                        </table>""".formatted(SalvarResultadosHTML.convertTableItemsToHtml(table));
    }

    private static String convertTableItemsToHtml(final Object[][] table) {
        return Arrays.stream(table)
                .map(SalvarResultadosHTML::convertItemToHtml)
                .collect(Collectors.joining("\n"));
    }

    private static String convertItemToHtml(final Object[] item) {
        return "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>"
                .formatted(item[0], item[1], item[2], item[3]);
    }

    /**
     * Stores charts' images and creates a html string connecting the files
     *
     * @param charts Array with the images to be saved together with the html
     */
    public void setCharts(final BufferedImage[] charts) {
        this.chartImages = Arrays.stream(charts)
                .filter(Objects::nonNull)
                .toArray(BufferedImage[]::new);

        this.chartsText = IntStream.range(0, this.chartImages.length)
                .mapToObj(SalvarResultadosHTML::makeImageTag)
                .collect(Collectors.joining("\n"));
    }

    private static String makeImageTag(final int cont) {
        return ("<img alt=\"\" src=\"chart%d.png\" style=\"width: 600px; " +
                "height: 300px;\" />\n").formatted(cont);
    }

    /**
     * Creates and stores internally a string with the global statistics.
     *
     * @param metrics Global simulation metrics
     */
    public void setMetricasGlobais(final MetricasGlobais metrics) {
        this.globalMetrics = """
                <li><strong>Total Simulated Time </strong>= %s</li>
                <li><strong>Satisfaction</strong> = %s %%</li>
                <li><strong>Idleness of processing resources</strong> = %s %%</li>
                <li><strong>Idleness of communication resources</strong> = %s %%</li>
                <li><strong>Efficiency</strong> = %s %%</li>
                %s
                <li><strong>Cost Total Processing</strong> = %s</li>
                <li><strong>Cost Total Memory</strong> = %s</li>
                <li><strong>Cost Total Disk</strong> = %s</li>
                """.formatted(
                metrics.getTempoSimulacao(),
                metrics.getSatisfacaoMedia(),
                metrics.getOciosidadeComputacao(),
                metrics.getOciosidadeComunicacao(),
                metrics.getEficiencia(),
                SalvarResultadosHTML.makeEfficiencyDescriptionFromMetrics(metrics),
                metrics.getCustoTotalProc(),
                metrics.getCustoTotalMem(),
                metrics.getCustoTotalDisco()
        );
    }

    private static String makeEfficiencyDescriptionFromMetrics(final MetricasGlobais metrics) {
        if (metrics.getEficiencia() > SalvarResultadosHTML.EFFICIENCY_GOOD) {
            return SalvarResultadosHTML.getEfficiencyDescription(
                    "GOOD", "00ff00");
        } else if (metrics.getEficiencia() > SalvarResultadosHTML.EFFICIENCY_BAD) {
            return SalvarResultadosHTML.getEfficiencyDescription(
                    "MEDIA", "");
        } else {
            return SalvarResultadosHTML.getEfficiencyDescription(
                    "BAD", "ff0000");
        }
    }

    private static String getEfficiencyDescription(
            final String text, final String color) {
        final var sb =
                new StringBuilder("<li><strong>Efficiency %s</strong></li>".formatted(text));

        if (!color.isEmpty()) {
            sb.insert("<li>".length(),
                    "<span style=\"color:#%s;\">".formatted(color));
        }

        return sb.toString();
    }

    /**
     * Creates a string with client metrics in the queue network
     */
    public void setMetricasTarefas(final Metricas metrics) {
        final double commQueueAvgTime =
                metrics.getTempoMedioFilaComunicacao();
        final double commAvgTime =
                metrics.getTempoMedioComunicacao();
        final double procQueueAvgTime =
                metrics.getTempoMedioFilaProcessamento();
        final double procAvgTime =
                metrics.getTempoMedioProcessamento();

        this.tasks = """
                <ul><li><h2>Tasks</h2><ul><li><strong>Communication</strong><ul>
                <li>Queue average time: %s seconds.</li>
                <li>Communication average time: %s seconds.</li>
                <li>System average time: %s seconds.</li>
                </ul></li><li><strong>Processing</strong><ul>
                <li>Queue average time: %s seconds.</li>
                <li>Processing average time: %s seconds.</li>
                <li>System average time: %s seconds.</li></ul></li></ul></li></ul>"""
                .formatted(commQueueAvgTime, commAvgTime,
                        commQueueAvgTime + commAvgTime,
                        procQueueAvgTime, procAvgTime,
                        procQueueAvgTime + procAvgTime);
    }

    /**
     * Creates a directory with all necessary files to open the html containing
     * the simulation results
     *
     * @param dir Directory to store the files
     */
    public void gerarHTML(final File dir) throws IOException {

        SalvarResultadosHTML.createDirIfNonexistent(dir);

        try (final var pw = new PrintWriter(new FileWriter(
                new File(dir, "result.html"),
                StandardCharsets.UTF_8)
        )) {
            pw.print(this.generateHtml());
        }

        for (int i = 0; i < this.chartImages.length; i++) {
            this.writeChartToFile(dir, i);
        }

        SalvarResultadosHTML.exportMissingImages(dir);
    }

    private static void createDirIfNonexistent(final File dir) throws IOException {
        if (dir.exists()) {
            return;
        }

        if (!dir.mkdir()) {
            throw new IOException("Could not create directory");
        }
    }

    /**
     * Makes the full text in html containing the results
     *
     * @return Complete html text
     */
    private String generateHtml() {
        return """
                <!DOCTYPE html>
                <html>
                    <head>
                        <title>Simulation Results</title>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                    </head>
                    <body background="fundo_html.jpg" style="background-position: top center; background-repeat: no-repeat;">
                        <h1 id="topo" style="text-align: center;">
                            <span style="color:#8b4513;">
                            <img alt="" src="Logo_iSPD_128.png" align="left" style="width: 70px; height: 70px;" />
                            Simulation Results</span>
                            <img alt="" src="Logo_UNESP.png" align="right" style="width: 70px; height: 70px;" />
                        </h1>
                        <hr /><br />
                        <div>
                            <a href="#global">Global metrics</a> <br/>
                            <a href="#table">Table of Resource</a> <br/>
                            <a href="#chart">Charts</a> <br/>
                        </div>
                        <hr />
                        <h2 id="global" style="text-align: center;">
                            Global metrics</h2>
                        %s%s        <div>
                            <a href="#topo">Inicio</a>
                        </div>
                        <hr />
                        <h2 id="table" style="text-align: center;">
                            Table of Resource
                        </h2>
                %s        <div>
                            <a href="#topo">Inicio</a>
                        </div>
                        <hr />
                        <h2 id="chart" style="text-align: center;">
                            Charts
                        </h2>
                        <p style="text-align: center;">
                        %s        </p>
                        <div>
                            <a href="#topo">Inicio</a>
                        </div>
                        <hr />
                        <p style="font-size:10px;">
                            <a href="http://gspd.dcce.ibilce.unesp.br/">GSPD</a></p>
                    </body>
                </html>""".formatted(
                this.globalMetrics,
                this.tasks,
                this.table,
                this.chartsText
        );
    }

    private void writeChartToFile(final File dir, final int i) throws IOException {
        final var out = new File(dir, "chart%d.png".formatted(i));
        ImageIO.write(this.chartImages[i], "png", out);
    }

    private static void exportMissingImages(final File dir) throws IOException {
        for (final var nameAndExtension : new String[][] {
                { "fundo_html", "jpg" },
                { "Logo_iSPD_128", "png" },
                { "Logo_UNESP", "png" },
        }) {
            final var file = nameAndExtension[0] + "." + nameAndExtension[1];
            final var out = new File(dir, file);

            if (!out.exists()) {
                final var img = ImageIO.read(Objects.requireNonNull(
                        MainWindow.class.getResource("imagens/" + file)));
                ImageIO.write(img, nameAndExtension[1], out);
            }
        }
    }

    /**
     * This method save the simulation results in the specified directory.
     *
     * @param directory the directory where the simulation results are saved.
     * @param charts the simulation chart maker
     */
    public void saveHtml(final File directory,
                          final SimulationResultChartMaker charts) {
        final var chartsImage = Stream.of(
                        charts.getProcessingBarChart(),
                        charts.getProcessingPieChart(),
                        charts.getCommunicationBarChart(),
                        charts.getCommunicationPieChart(),
                        charts.getComputingPowerPerMachineChart(),
                        charts.getComputingPowerPerTaskChart(),
                        charts.getComputingPowerPerUserChart()
                )
                .filter(Objects::nonNull)
                .map((chart) -> chart.getChart().createBufferedImage(1200, 600))
                .toArray(BufferedImage[]::new);

        this.setCharts(chartsImage);

        try {
            this.gerarHTML(directory);
        } catch (final IOException e) {
            throw new RuntimeException("An error occurred to generate the " +
                    "HTML file containing the simulation results.", e);
        }
    }
}
