package problem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * Class used for parsing scenarios written in XML
 * @author cpralet
 *
 */
public class ProblemParserXML {

	Map<String, User> users;
	Map<String, Satellite> satellites;
	Map<String, Station> stations;
	double horizonStart;
	double horizonEnd;
	
	public ProblemParserXML(){
		users = new HashMap<String,User>();
		satellites = new HashMap<String,Satellite>();
		stations = new HashMap<String,Station>();	
	}
	
	public PlanningProblem read(String filenameSystemData, String filenamePlanningData) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError{
		PlanningProblem pb = new PlanningProblem();

		readSystemData(pb, filenameSystemData);
		readPlanningData(pb, filenamePlanningData);
		return pb;
	}
	
	public void readSystemData(PlanningProblem pb, String filenameSystemData) throws XMLStreamException, FileNotFoundException, FactoryConfigurationError{
		// Create an XML reader
		XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(filenameSystemData));
		QName nameAttibute = new QName("name");		
		QName quotaAttibute = new QName("quota");

		// Read the XML file
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();			
			if (event.isStartElement()){
				StartElement sevent = event.asStartElement(); 
				String eventName = sevent.getName().getLocalPart();
				if(eventName.equals("user")) {
					String name = sevent.getAttributeByName(nameAttibute).getValue();
					double quota = Double.parseDouble(sevent.getAttributeByName(quotaAttibute).getValue());
					User user = pb.addUser(name,quota);
					users.put(name, user);
				}
				else if(eventName.equals("satellite")) {
					String name = sevent.getAttributeByName(nameAttibute).getValue();
					Satellite satellite = pb.addSatellite(name);
					satellites.put(name, satellite);
				}
				else if(eventName.equals("station")){
					String name = sevent.getAttributeByName(nameAttibute).getValue();
					Station station = pb.addStation(name);
					stations.put(name, station);
				}				
			}
		}
		eventReader.close();
	}

	public void readPlanningData(PlanningProblem pb, String filenamePlanningData) throws XMLStreamException, FileNotFoundException, FactoryConfigurationError{
		// Create an XML reader
		XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(filenamePlanningData));
		QName nameAttibute = new QName("name");
		QName satelliteAttribute = new QName("satellite");
		QName stationAttribute = new QName("station");
		QName durationAttribute = new QName("duration");
		QName earliestStartAttribute = new QName("earliestStartTime");
		QName latestStartAttribute = new QName("latestStartTime");
		QName zenithAngleAttribute = new QName("zenithAngle");
		QName rollAngleAttribute = new QName("rollAngle");
		QName startTimeAttribute = new QName("startTime");
		QName endTimeAttribute = new QName("endTime");
		QName priorityAttribute = new QName("priority");
		QName userAttribute = new QName("user");
		QName cloudProbaAttribute = new QName("cloudProba");
		QName volumeAttribute = new QName("volume");
		QName acquisitionTimeAttribute = new QName("acquisitionTime");
		QName idAttribute = new QName("id");
		QName startAttibute = new QName("start");
		QName endAttibute = new QName("end");
		
		// Read the XML file
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()){
				StartElement sevent = event.asStartElement(); 
				String eventName = sevent.getName().getLocalPart(); 
				if(eventName.equals("horizon")){
					horizonStart = Double.parseDouble(sevent.getAttributeByName(startAttibute).getValue());
					horizonEnd = Double.parseDouble(sevent.getAttributeByName(endAttibute).getValue());
					pb.setHorizon(horizonStart, horizonEnd);
				}
				else if(eventName.equals("candidateAcquisitions")) {

					while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("candidateAcquisitions"))){
						event = eventReader.nextEvent();

						if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("candidateAcquisition")) {
							sevent = event.asStartElement(); 
							String requestName = sevent.getAttributeByName(nameAttibute).getValue();
							int priority = Integer.parseInt(sevent.getAttributeByName(priorityAttribute).getValue());
							User user = users.get(sevent.getAttributeByName(userAttribute).getValue());
							CandidateAcquisition candidateAcquisition = pb.addCandidateAcquisition(requestName, user, priority);
							// read all atomic requests associated with the request

							while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("candidateAcquisition"))){
								event = eventReader.nextEvent();
								if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("acquisitionOpportunity")) {
									sevent = event.asStartElement();
									Satellite satellite = satellites.get(sevent.getAttributeByName(satelliteAttribute).getValue());
									double earliestStart = Double.parseDouble(sevent.getAttributeByName(earliestStartAttribute).getValue());
									earliestStart = Math.max(earliestStart, horizonStart);
									double latestStart = Double.parseDouble(sevent.getAttributeByName(latestStartAttribute).getValue());
									latestStart = Math.min(latestStart, horizonEnd);
									if(earliestStart <= latestStart){
										double zenithAngle = Double.parseDouble(sevent.getAttributeByName(zenithAngleAttribute).getValue());
										double rollAngle = Double.parseDouble(sevent.getAttributeByName(rollAngleAttribute).getValue());
										double cloudProba = Double.parseDouble(sevent.getAttributeByName(cloudProbaAttribute).getValue());
										double duration = Double.parseDouble(sevent.getAttributeByName(durationAttribute).getValue());				
										long volume = Long.parseLong(sevent.getAttributeByName(volumeAttribute).getValue());

										candidateAcquisition.addAcqOpportunity(satellite, earliestStart, latestStart, duration, zenithAngle, rollAngle, cloudProba, volume);							
									}									
								}		
							}					
						}
					}
				}
				else if(eventName.equals("recordedAcquisitions")){					
					while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("recordedAcquisitions"))){
						event = eventReader.nextEvent();
						if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("recordedAcquisition")) {
							sevent = event.asStartElement();
							Satellite satellite = satellites.get(sevent.getAttributeByName(satelliteAttribute).getValue());
							User user = users.get(sevent.getAttributeByName(userAttribute).getValue());
							int acquisitionId = Integer.parseInt(sevent.getAttributeByName(idAttribute).getValue());							
							int priority = Integer.parseInt(sevent.getAttributeByName(priorityAttribute).getValue());
							double acquisitionTime = Double.parseDouble(sevent.getAttributeByName(acquisitionTimeAttribute).getValue());		
							long volume = Long.parseLong(sevent.getAttributeByName(volumeAttribute).getValue());
							pb.addRecordedAcquisition(acquisitionId,"REC_"+acquisitionId,user,priority,satellite, acquisitionTime, volume);																					
						}
					}					
				}
				else if(eventName.equals("downloadWindows")){
					while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("downloadWindows"))){
						event = eventReader.nextEvent();
						if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("downloadWindow")) {
							sevent = event.asStartElement();
							Satellite satellite = satellites.get(sevent.getAttributeByName(satelliteAttribute).getValue());
							Station station = stations.get(sevent.getAttributeByName(stationAttribute).getValue());
							double startTime = Double.parseDouble(sevent.getAttributeByName(startTimeAttribute).getValue());
							startTime = Math.max(startTime, horizonStart);
							double endTime = Double.parseDouble(sevent.getAttributeByName(endTimeAttribute).getValue());
							endTime = Math.min(endTime, horizonEnd);
							if(startTime <= endTime){
								pb.addDownloadWindow(satellite, station, startTime, endTime);
							}
						}
					}

				}
			}
		}
		eventReader.close();
	}

}

