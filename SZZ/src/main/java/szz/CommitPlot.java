package szz;

import org.eclipse.egit.github.core.Issue;
import org.jfree.chart.*;
import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.*;
import org.jfree.ui.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class CommitPlot extends ApplicationFrame  {

    public XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
    public int fixCommitSize;
    public  Commit[][] bugLines = new Commit[10000][500];
    public  Issue[][] issueLine=new Issue[10000][500];
    public static String user=null;
    public static String repositoryName=null;





    public CommitPlot(String applicationTitle, String chartTitle, Map<Commit, List<Commit>> blameMap , int fixCommits, Map<Commit, List<Issue>> issueMap, String user, String repositoryName ) throws ParseException, IOException {

        super(applicationTitle);
        this.user=user;
        this.repositoryName=repositoryName;

        XYDataset dataset= createDataset(blameMap,issueMap);
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                chartTitle ,
                "Commits" ,
                "" ,
                dataset,
                PlotOrientation.VERTICAL ,
                false , true , false);
        ChartPanel chartPanel = new ChartPanel( xylineChart );
        chartPanel.setPreferredSize( new Dimension( 800 , 500 ) );
        chartPanel.setForeground(Color.black);
        chartPanel.setDismissDelay(1200000*5);
        chartPanel.setInitialDelay(0);
        chartPanel.setReshowDelay(0);



        setContentPane(chartPanel);
        XYShapeAnnotation annotation = new XYShapeAnnotation(new Ellipse2D.Float(100.0f, 100.0f, 100.0f, 100.0f), new BasicStroke(1.0f), Color.blue);
        XYPointerAnnotation pointer = new XYPointerAnnotation("arrow", 0.5,0.5,0.0);
        XYPlot plot = xylineChart.getXYPlot( );
        plot.addAnnotation(pointer);
        plot.addAnnotation(annotation);
//        StandardXYToolTipGenerator ttG =
//                new StandardXYToolTipGenerator("", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"), new DecimalFormat("0.00") );

        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("MMM yyyy"));
        plot.setDomainAxis(dateAxis);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        Shape cross = ShapeUtilities.createDiamond(4);

        chartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                XYItemEntity xyEntity = (XYItemEntity) event.getEntity();
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                String url=null;
                if((2*fixCommitSize-1< series)&&(series<fixCommitSize*3)){
                    int index= series-fixCommitSize*2;
                    url= issueLine[index][item].getUrl();
                    System.out.println(url);
                }else {
                    String hashId = bugLines[series][item].getId();
                    url = "http://github.com/" + user + "/" + repositoryName + "/" + "commit" + "/" + hashId;
                }
                if(Desktop.isDesktopSupported()){
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(new URI(url));
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }else{
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("xdg-open " + url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {

            }
        });

        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.
                OUTSIDE8, TextAnchor.CENTER));
        Commit[][] bugs = new Commit[10000][500];
        int countFix=0;
        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()) {
            Commit key = entry.getKey();
            Set<Commit> dedupeCommit = new HashSet<>();
            dedupeCommit.add(key);

            for (Commit value : entry.getValue()) {
                dedupeCommit.add(value);
            }
            int all=0;
            for(Commit commit:dedupeCommit){
                bugs[countFix][all]=commit;
                all++;
            }
            countFix++;
        }
        bugLines=bugs;
        Issue[][]issueArray= new Issue[1000][500];
        int issueCount=0;
        for(Map.Entry<Commit, List<Issue>> map: issueMap.entrySet()) {
            Set<Issue> dedupeCommit = new HashSet<>();
            for (Issue value : map.getValue()) {
                dedupeCommit.add(value);
            }
            int all=0;
            for(Issue issue:dedupeCommit){
                issueArray[issueCount][all]=issue;
                //System.out.println("issue no"+issue.getNumber()+"  "+issue.getCreatedAt());
                all++;
            }
            issueCount++;
        }
        issueLine= issueArray;
        System.out.println("issuearray size  "+issueArray.length+"  fixcommitsSize  "+ fixCommitSize+" series "+plot.getSeriesCount());
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(){
            @Override
            public String generateLabelString(XYDataset dataset, int series, int item) {
                String label=null;
                if ((series<fixCommitSize )) {
                    Commit commit = bugs[series][item];

                    label = "<html>" + "<body bgcolor=\"#C0C0C0\">" + "<h4 style=\"color:#3C3C3C;\">" + "&nbsp Commit-Date: " +
                            new Date((long) commit.getGitCommit().getCommitTime() * 1000) + "<br>" + "&nbsp Commit-Id: " +
                            commit.getGitCommit().getName() + "<br>" + "&nbsp Author: " + commit.getGitCommit().getAuthorIdent().getName() + "<br>" +
                            "&nbsp Commit-message: " + commit.getGitCommit().getFullMessage() + "</h4>" + "</body>" + "</html>";

                }else if((2*fixCommitSize-1< series)&&(series<fixCommitSize*3)){
                    int index= series-fixCommitSize*2;
                    label = "<html>" + "<body bgcolor=\"#C0C0C0\">" + "<h4 style=\"color:#3C3C3C;\">" + "&nbsp Issue-no: " +
                            issueArray[index][item].getNumber()+"<br>"+"&nbsp Created:  "+
                            new Date(issueArray[index][item].getCreatedAt().getTime()) +
                            "<br>" + "&nbsp Closed At: " + new Date(issueArray[index][item].getClosedAt().getTime())+ "<br>"+
                            "&nbsp URL: "+issueArray[index][item].getUrl()+"<br>"+"&nbsp Title: " +issueArray[index][item].getTitle()
                            +"</h4>" + "</body>" + "</html>";

                }else {
                    label=null;
                }
                return label;

            }
        });
        renderer.setBaseShapesFilled(true);
        renderer.setBaseShapesVisible(true);
        renderer.setDefaultEntityRadius(3);
//        renderer.setStroke(new BasicStroke(2f,
//                BasicStroke.CAP_BUTT,
//                BasicStroke.JOIN_BEVEL));
        ValueAxis range = plot.getRangeAxis();
        ValueAxis rangeD= plot.getDomainAxis();
        range.setVisible(false);
        rangeD.setVisible(true);
        range.setRange(0,plot.getSeriesCount()/3+1);
        Graphics2D g2;
        int orig=0;
        int fix=0;
        int issue=0;
        for(int i=0;i<plot.getSeriesCount();i++){
            if(i<fixCommitSize){
                renderer.setSeriesShape(i,cross);
                renderer.setSeriesPaint(i,Color.BLACK);
                renderer.setUseFillPaint(true);
                renderer.setSeriesFillPaint(i,Color.GRAY);
                orig++;
            }else if((fixCommitSize<= i)&&(i<fixCommitSize*2)){
                renderer.setSeriesShape(i,new Ellipse2D.Double(-5, -5, 9, 9));
                renderer.setSeriesPaint(i,Color.BLUE);
                renderer.setSeriesFillPaint(i,Color.BLUE);
                renderer.setSeriesLinesVisible(i,false);
                fix++;
            } else if((i>=fixCommitSize*2)){

                renderer.setSeriesShape(i,new Ellipse2D.Double(-7, -7, 12, 12));
                renderer.setSeriesPaint(i,Color.RED);
                renderer.setSeriesFillPaint(i,Color.RED);
                //renderer.setSeriesShapesFilled(i,false);
                renderer.setSeriesLinesVisible(i,true);
                issue++;
                System.out.println(issue);
            }
        }
        System.out.println("orig  "+orig+"fix  "+fix+"issue  "+issue);
        plot.setRenderer(renderer);
        setContentPane(chartPanel);
        SVGGraphics2D svg = new SVGGraphics2D(1500, 900);
        Rectangle r = new Rectangle(0, 0, 1500, 900);
        xylineChart.draw(svg, r);
        File f = new File("CommitsBugInduce.svg");
        SVGUtils.writeToSVG(f, svg.getSVGElement());
    }


    private XYDataset createDataset(Map<Commit, List<Commit>>  blameMap, Map<Commit, List<Issue>> issueMap ) throws ParseException {

        String name;
        int count=0;
        int yValue=1;
        XYSeriesCollection dataset = new XYSeriesCollection( );
        //TimeSeriesCollection dataset=new TimeSeriesCollection();
        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()) {
            Commit key = entry.getKey();
            Set<Commit> dedupeCommit = new HashSet<>();
            XYSeries seriesCommits = new XYSeries( "series"+count,false,true);
            //TimeSeries seriesCommits = new TimeSeries("series"+count);
            count++;
            dedupeCommit.add(key);
            for (Commit value : entry.getValue()) {
                dedupeCommit.add(value);
            }
            for(Commit commit:dedupeCommit){

                seriesCommits.add(commit.getGitCommit().getCommitterIdent().getWhen().getTime(),yValue);
            }
            yValue++;
            seriesCommits.getAutoSort();

            dataset.addSeries(seriesCommits);
        }
        int yValue1=1;
        Set<Commit> dedupeCommit1 = new HashSet<>();
        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()) {
            Commit key = entry.getKey();
            dedupeCommit1.add(key);
        }
        for(Commit commit:dedupeCommit1){
            XYSeries seriesCommits = new XYSeries( "series"+count,false,true);
            count++;
            seriesCommits.add(commit.getGitCommit().getCommitterIdent().getWhen().getTime(),yValue1);
            yValue1++;
            dataset.addSeries(seriesCommits);
        }
        fixCommitSize=dedupeCommit1.size();
        int yValue2=1;
        System.out.println("Issue Size  "+issueMap.size()+"fixCommitSize  "+fixCommitSize);
        for(Map.Entry<Commit, List<Issue>> map: issueMap.entrySet()) {
            Commit key = map.getKey();
            List<Issue> isuueList = new ArrayList<>();
            XYSeries seriesCommits = new XYSeries( "series"+count,false,true);
            //TimeSeries seriesCommits = new TimeSeries("series"+count);
            count++;
            for (Issue value : map.getValue()) {
                isuueList.add(value);
            }
            for(Issue issue:isuueList){
                seriesCommits.add(issue.getCreatedAt().getTime(),yValue2);
                //seriesCommits.add(issue.getClosedAt().getTime(),yValue2);
                System.out.println("issue no"+issue.getNumber()+"  "+issue.getCreatedAt());
            }
            yValue2++;
            seriesCommits.getAutoSort();
            dataset.addSeries(seriesCommits);
        }
        return dataset;
    }

    public static  void showPlot(Map<Commit, List<Commit>>  blameMap, int fixCommits, Map<Commit, List<Issue>> issueMap ) throws ParseException, IOException {
        CommitPlot chart = new CommitPlot("Bug Inducing relationship", "",blameMap, fixCommits,issueMap,user,repositoryName);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }
//MouseEvent mEvent = event.getTrigger();	String strTemp = xPanel.getToolTipText(mEvent);

}