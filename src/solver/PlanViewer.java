package solver;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import params.Params;
import problem.Acquisition;
import problem.CandidateAcquisition;
import problem.DownloadWindow;
import problem.PlanningProblem;
import problem.Satellite;
import problem.ProblemParserXML;
import problem.Station;


/**
 * Class useful for defining the GanttChart representation of the plan
 * @author cpralet
 *
 */
public class PlanViewer {

	/** Satellite associated with this plan view */
	final Satellite satellite;
	
	/** Earliest time value in the Gantt */
	protected final Date ganttStart;

	/** Latest time value in the Gantt */
	protected final Date ganttEnd;

	/** Dataset associated with the GanttChart*/
	protected TaskSeriesCollection dataset;

	/** Panel containing the GanttChart */
	public ChartPanel chartPanel;

	SolutionPlan plan;

	Task satelliteTask;
	Map<Station,Task> stationVisTasks;
	Map<Station,Task> stationDlTasks;
	SimpleDateFormat sdf;
	Date refDate;
	long refDateSeconds;

	/** 
	 * @param planner Planner whose solution is depicted by the viewer
	 * @throws ParseException 
	 */
	public PlanViewer(SolutionPlan plan, Satellite satellite) throws ParseException {
		this.plan = plan;
		this.satellite = satellite;
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		refDate = sdf.parse("07-05-2004 12:22:35");
		refDateSeconds = refDate.getTime();

		stationVisTasks = new HashMap<Station,Task>();
		stationDlTasks = new HashMap<Station,Task>();

		ganttStart = getDate(plan.pb.horizonStart); 
		ganttEnd = getDate(plan.pb.horizonEnd);

		// create data set
		createSampleDataset(plan);

		// Creation of GanttChart
		JFreeChart chart = ChartFactory.createGanttChart(
				"",  // chart title
				"",              // domain axis label
				"",              // range axis label
				dataset,// data
				true,                // include legend
				true,                // tooltips
				false                // urls
				);
		chart.removeLegend();

		// some updates of the plot associated with the chart
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.gray);	    

		DateAxis xaxis = (DateAxis) plot.getRangeAxis(); 
		xaxis.setTimeZone(TimeZone.getTimeZone("GMT"));
		//xaxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		//xaxis.setDateFormatOverride(new SimpleDateFormat("SS"));


		MyRenderer r = new MyRenderer(plan, dataset);
		r.setBaseItemLabelsVisible(true);
		r.setBaseItemLabelPaint(Color.BLACK);
		r.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE6, TextAnchor.BOTTOM_CENTER));
		plot.setRenderer(r);

		// create a panel containing the chart
		chartPanel = new ChartPanel(chart,false);
	}

	/**
	 * 
	 * @param t time in seconds from J2000
	 * @return the date associated with t
	 */
	public Date getDate(double secondsSinceRefDate){		
		return new Date((long) (1000*(refDateSeconds+secondsSinceRefDate)));		
	}

	/**
	 * Creates a dataset for a Gantt chart representing the schedule
	 */
	public void createSampleDataset(SolutionPlan plan) {


		Date date_start_all = ganttStart;
		Date date_end_all = ganttEnd;

		dataset = new TaskSeriesCollection();

		TaskSeries tasksSeries = new TaskSeries("Planned");

		// add the satellite timeline
		satelliteTask = new Task(satellite.name, date_start_all, date_end_all);
		satelliteTask.addSubtask(new Task("ACQ_IniSatellite"+satellite.name, date_start_all, date_start_all));				
		tasksSeries.add(satelliteTask);		

		stationVisTasks = new HashMap<Station,Task>();
		stationDlTasks = new HashMap<Station,Task>();
		for(Station station : plan.pb.stations){

			Task tDl = new Task(station.name + " [DL]", date_start_all, date_end_all);
			tDl.addSubtask(new Task("DL_InitStation"+station.name, date_start_all, date_start_all));	
			stationDlTasks.put(station,tDl);
			tasksSeries.add(tDl);
			
			Task tVis = new Task(station.name + " [VIS]", date_start_all, date_end_all);
			stationVisTasks.put(station,tVis);
			tasksSeries.add(tVis);

			for(DownloadWindow w : plan.pb.downloadWindows){
				if(w.satellite == satellite && w.station == station){
					tVis.addSubtask(new Task("VIS_"+w.satellite.name, getDate(w.start), getDate(w.end)));
				}
			}
			
			tVis.addSubtask(new Task("VIS_InitStation"+station.name, date_start_all, date_start_all));					
			tDl.addSubtask(new Task("DL_InitStation"+station.name, date_start_all, date_start_all));			
		}

		// add the acquisition plan
		for(CandidateAcquisition a : plan.plannedAcquisitions){
			String name = a.name;
			if(a.selectedAcquisitionWindow.satellite == satellite)
				satelliteTask.addSubtask(new Task("ACQ_"+name, getDate(a.selectedAcquisitionStartTime), getDate(a.selectedAcquisitionEndTime)));			
		}

		// add the download plan
		for(Acquisition a : plan.plannedDownload){
			String name = a.name;
			DownloadWindow downloadWindow = a.selectedDownloadWindow;
			if(downloadWindow.satellite == satellite)
				stationDlTasks.get(downloadWindow.station).addSubtask(new Task("DL_"+name, getDate(a.selectedDownloadStartTime), getDate(a.selectedDownloadEndTime)));			
		}
		dataset.add(tasksSeries);
	}


	public void show(){
		JFrame frame = new JFrame("Schedule " + satellite.name);		
		frame.addWindowListener(
				new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				}
				);	
		frame.getContentPane().add(chartPanel,"Center");
		frame.pack();
		chartPanel.validate();
		chartPanel.repaint();
		frame.setSize(950,600);
		frame.setResizable(true);
		frame.setVisible(true);
	}

	private class MyRenderer extends GanttRenderer {

		Random rand = new Random(0);
		
		private TaskSeriesCollection dataset;

		public MyRenderer(SolutionPlan plan, TaskSeriesCollection dataset){
			super();
			this.dataset = dataset;
		}


		public Paint getItemPaint(int row, int col, int sub) {			
			TaskSeries series = (TaskSeries) dataset.getRowKeys().get(row);
			String s = ((List<Task>)series.getTasks()).get(col).getSubtask(sub).getDescription();
			if(s.startsWith("VIS")) // case visibility window
				return Color.GREEN;			
			if(s.startsWith("DL")) // case download window
				return new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()); //Color.RED;			
			else if(s.startsWith("ACQ")){ // case acquisition
				return new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()); //Color.ORANGE;
			}
			else{ // case unknown label
				throw new RuntimeException("Unknown label");				
			}
		}

		protected void drawTasks(Graphics2D g2,
				CategoryItemRendererState state,
				Rectangle2D dataArea,
				CategoryPlot plot,
				CategoryAxis domainAxis,
				ValueAxis rangeAxis,
				GanttCategoryDataset dataset,
				int row,
				int column) {

			int count = dataset.getSubIntervalCount(row, column);
			if (count == 0) {
				drawTask(g2, state, dataArea, plot, domainAxis, rangeAxis, 
						dataset, row, column);
			}

			for (int subinterval = 0; subinterval < count; subinterval++) {

				RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

				// value 0
				Number value0 = dataset.getStartValue(row, column, subinterval);
				if (value0 == null) {
					return;
				}
				double translatedValue0 = rangeAxis.valueToJava2D(
						value0.doubleValue(), dataArea, rangeAxisLocation);

				// value 1
				Number value1 = dataset.getEndValue(row, column, subinterval);
				if (value1 == null) {
					return;
				}
				double translatedValue1 = rangeAxis.valueToJava2D(
						value1.doubleValue(), dataArea, rangeAxisLocation);

				if (translatedValue1 < translatedValue0) {
					double temp = translatedValue1;
					translatedValue1 = translatedValue0;
					translatedValue0 = temp;
				}

				double rectStart = calculateBarW0(plot, plot.getOrientation(), 
						dataArea, domainAxis, state, row, column);
				double rectLength = Math.abs(translatedValue1 - translatedValue0);
				double rectBreadth = state.getBarWidth();

				// DRAW THE BARS...
				Rectangle2D bar = null;

				if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
					bar = new Rectangle2D.Double(translatedValue0, rectStart, 
							rectLength, rectBreadth);
				}
				else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
					bar = new Rectangle2D.Double(rectStart, translatedValue0, 
							rectBreadth, rectLength);
				}

				Rectangle2D completeBar = null;
				Rectangle2D incompleteBar = null;
				Number percent = dataset.getPercentComplete(row, column, 
						subinterval);
				double start = getStartPercent();
				double end = getEndPercent();
				if (percent != null) {
					double p = percent.doubleValue();
					if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
						completeBar = new Rectangle2D.Double(translatedValue0, 
								rectStart + start * rectBreadth, rectLength * p, 
								rectBreadth * (end - start));
						incompleteBar = new Rectangle2D.Double(translatedValue0 
								+ rectLength * p, rectStart + start * rectBreadth, 
								rectLength * (1 - p), rectBreadth * (end - start));
					}
					else if (plot.getOrientation() == PlotOrientation.VERTICAL) {
						completeBar = new Rectangle2D.Double(rectStart + start 
								* rectBreadth, translatedValue0 + rectLength 
								* (1 - p), rectBreadth * (end - start), 
								rectLength * p);
						incompleteBar = new Rectangle2D.Double(rectStart + start 
								* rectBreadth, translatedValue0, rectBreadth 
								* (end - start), rectLength * (1 - p));
					}

				}

				Paint seriesPaint = getItemPaint(row, column, subinterval);
				g2.setPaint(seriesPaint);
				g2.fill(bar);

				if (completeBar != null) {
					g2.setPaint(getCompletePaint());
					g2.fill(completeBar);
				}
				if (incompleteBar != null) {
					g2.setPaint(getIncompletePaint());
					g2.fill(incompleteBar);
				}
				if (isDrawBarOutline() 
						&& state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
					g2.setStroke(getItemStroke(row, column));
					g2.setPaint(getItemOutlinePaint(row, column));
					g2.draw(bar);
				}

				MyCategoryItemLabelGenerator generator = new MyCategoryItemLabelGenerator();
				if (generator != null && isItemLabelVisible(row, column)) {
					drawItemLabel(g2, dataset, row, column,subinterval, plot, generator, bar, false);
				}

				// collect entity and tool tip information...
				if (state.getInfo() != null) {
					EntityCollection entities = state.getEntityCollection();
					if (entities != null) {
						String tip = null;
						if (getToolTipGenerator(row, column) != null) {
							tip = getToolTipGenerator(row, column).generateToolTip(
									dataset, row, column);
						}
						String url = null;
						if (getItemURLGenerator(row, column) != null) {
							url = getItemURLGenerator(row, column).generateURL(
									dataset, row, column);
						}
						CategoryItemEntity entity = new CategoryItemEntity(
								bar, tip, url, dataset, dataset.getRowKey(row), 
								dataset.getColumnKey(column));
						entities.add(entity);
					}
				}
			}
		}


		private void drawItemLabel(Graphics2D g2,
				CategoryDataset data,
				int row,
				int column,
				int sub,
				CategoryPlot plot,
				MyCategoryItemLabelGenerator generator,
				Rectangle2D bar,
				boolean negative) {

			String label = generator.generateLabel(data, row, column, sub);
			if (label == null) {
				return;  // nothing to do
			}

			Font labelFont = getItemLabelFont(row, column);
			g2.setFont(labelFont);
			Paint paint = getItemLabelPaint(row, column);
			g2.setPaint(paint);

			// find out where to place the label...
			ItemLabelPosition position = null;
			if (!negative) {
				position = getPositiveItemLabelPosition(row, column);
			}
			else {
				position = getNegativeItemLabelPosition(row, column);
			}

			// work out the label anchor point...
			Point2D anchorPoint = calculateLabelAnchorPoint(position.getItemLabelAnchor(), bar, plot.getOrientation());



			if (isInternalAnchor(position.getItemLabelAnchor())) {
				Shape bounds = TextUtilities.calculateRotatedStringBounds(label,
						g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(),
						position.getTextAnchor(), position.getAngle(),
						position.getRotationAnchor());

				if (bounds != null) {
					if (!bar.contains(bounds.getBounds2D())) {
						if (!negative) {
							position = getPositiveItemLabelPositionFallback();
						}
						else {
							position = getNegativeItemLabelPositionFallback();
						}
						if (position != null) {
							anchorPoint = calculateLabelAnchorPoint(
									position.getItemLabelAnchor(), bar,
									plot.getOrientation());
						}
					}
				}

			}

			if (position != null) {
				TextUtilities.drawRotatedString(label, g2,
						(float) anchorPoint.getX(), (float) anchorPoint.getY(),
						position.getTextAnchor(), position.getAngle(),
						position.getRotationAnchor());
			}
		}


		private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor,
				Rectangle2D bar, PlotOrientation orientation) {

			Point2D result = null;
			double offset = getItemLabelAnchorOffset();
			double x0 = bar.getX() - offset;
			double x1 = bar.getX();
			double x2 = bar.getX() + offset;
			double x3 = bar.getCenterX();
			double x4 = bar.getMaxX() - offset;
			double x5 = bar.getMaxX();
			double x6 = bar.getMaxX() + offset;

			double y0 = bar.getMaxY() + offset;
			double y1 = bar.getMaxY();
			double y2 = bar.getMaxY() - offset;
			double y3 = bar.getCenterY();
			double y4 = bar.getMinY() + offset;
			double y5 = bar.getMinY();
			double y6 = bar.getMinY() - offset;

			if (anchor == ItemLabelAnchor.CENTER) {
				result = new Point2D.Double(x3, y3);
			}
			else if (anchor == ItemLabelAnchor.INSIDE1) {
				result = new Point2D.Double(x4, y4);
			}
			else if (anchor == ItemLabelAnchor.INSIDE2) {
				result = new Point2D.Double(x4, y4);
			}
			else if (anchor == ItemLabelAnchor.INSIDE3) {
				result = new Point2D.Double(x4, y3);
			}
			else if (anchor == ItemLabelAnchor.INSIDE4) {
				result = new Point2D.Double(x4, y2);
			}
			else if (anchor == ItemLabelAnchor.INSIDE5) {
				result = new Point2D.Double(x4, y2);
			}
			else if (anchor == ItemLabelAnchor.INSIDE6) {
				result = new Point2D.Double(x3, y2);
			}
			else if (anchor == ItemLabelAnchor.INSIDE7) {
				result = new Point2D.Double(x2, y2);
			}
			else if (anchor == ItemLabelAnchor.INSIDE8) {
				result = new Point2D.Double(x2, y2);
			}
			else if (anchor == ItemLabelAnchor.INSIDE9) {
				result = new Point2D.Double(x2, y3);
			}
			else if (anchor == ItemLabelAnchor.INSIDE10) {
				result = new Point2D.Double(x2, y4);
			}
			else if (anchor == ItemLabelAnchor.INSIDE11) {
				result = new Point2D.Double(x2, y4);
			}
			else if (anchor == ItemLabelAnchor.INSIDE12) {
				result = new Point2D.Double(x3, y4);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE1) {
				result = new Point2D.Double(x5, y6);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE2) {
				result = new Point2D.Double(x6, y5);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE3) {
				result = new Point2D.Double(x6, y3);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE4) {
				result = new Point2D.Double(x6, y1);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE5) {
				result = new Point2D.Double(x5, y0);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE6) {
				result = new Point2D.Double(x3, y0);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE7) {
				result = new Point2D.Double(x1, y0);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE8) {
				result = new Point2D.Double(x0, y1);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE9) {
				result = new Point2D.Double(x0, y3);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE10) {
				result = new Point2D.Double(x0, y5);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE11) {
				result = new Point2D.Double(x1, y6);
			}
			else if (anchor == ItemLabelAnchor.OUTSIDE12) {
				result = new Point2D.Double(x3, y6);
			}

			return result;

		}


		/**
		 * Returns <code>true</code> if the specified anchor point is inside a bar.
		 * 
		 * @param anchor
		 *            the anchor point.
		 * 
		 * @return A boolean.
		 */
		private boolean isInternalAnchor(ItemLabelAnchor anchor) {
			return anchor == ItemLabelAnchor.CENTER
					|| anchor == ItemLabelAnchor.INSIDE1
					|| anchor == ItemLabelAnchor.INSIDE2
					|| anchor == ItemLabelAnchor.INSIDE3
					|| anchor == ItemLabelAnchor.INSIDE4
					|| anchor == ItemLabelAnchor.INSIDE5
					|| anchor == ItemLabelAnchor.INSIDE6
					|| anchor == ItemLabelAnchor.INSIDE7
					|| anchor == ItemLabelAnchor.INSIDE8
					|| anchor == ItemLabelAnchor.INSIDE9
					|| anchor == ItemLabelAnchor.INSIDE10
					|| anchor == ItemLabelAnchor.INSIDE11
					|| anchor == ItemLabelAnchor.INSIDE12;
		}




		class MyCategoryItemLabelGenerator implements CategoryItemLabelGenerator{

			@Override
			public String generateColumnLabel(CategoryDataset dataset, int column) {
				return dataset.getColumnKey(column).toString();
			}

			@Override
			public String generateRowLabel(CategoryDataset dataset, int row) {
				return dataset.getRowKey(row).toString();
			}

			@Override
			public String generateLabel(CategoryDataset dataset, int row, int column) {
				return null;
			}

			public String generateLabel(CategoryDataset dataSet, int row, int col, int sub) {
				TaskSeries series = (TaskSeries) dataSet.getRowKeys().get(row);
				List<Task> tasks = series.getTasks(); // unchecked
				return tasks.get(col).getSubtask(sub).getDescription();				
			}
		}
	}
	
	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException, ParseException{
		
		ProblemParserXML parser = new ProblemParserXML(); 
		PlanningProblem pb = parser.read(Params.systemDataFile,Params.planningDataFile);
		SolutionPlan plan = new SolutionPlan(pb);
		plan.readAcquisitionPlan("output/solutionAcqPlan_SAT1.txt");
		plan.readAcquisitionPlan("output/solutionAcqPlan_SAT2.txt");
		plan.readDownloadPlan("output/downloadPlan.txt");
		
		for(Satellite satellite : pb.satellites){
			PlanViewer planView = new PlanViewer(plan,satellite);
			planView.show();
		}
	}

}
