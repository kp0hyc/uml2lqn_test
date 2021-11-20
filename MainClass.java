package uml2lqn_gen;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainClass {
	private Map<String, String> class_id_map = new HashMap<String, String>();
	private Map<String, String> class_prop_map = new HashMap<String, String>();
	
	private class Task {
		public String name;
		public String proc;
		public int multiplicity;
	}

	private class CommPath {
		public String proc1;
		public String proc2;
		public String linkSpeed;
	}
	private void configurePlatform(Document doc) {
		NodeList list = doc.getElementsByTagName("packagedElement");
		
		List<Task> tasks = new ArrayList<Task>();
		List<CommPath> commPaths = new ArrayList<CommPath>();
		
        for (int temp = 0; temp < list.getLength(); temp++) {

              Node node = list.item(temp);

              if (node.getNodeType() == Node.ELEMENT_NODE) {

                  Element element = (Element) node;

                  String type = element.getAttribute("xmi:type");
                  
                  if (type.equals("uml:Device")) {
                	  
                	  String procName = element.getAttribute("name");
                	  int multiplicity = 1;
                	  String schedulingFlag = "f";
                	  String processorSpeed = "1.0";
                      
                	  NodeList comms = element.getElementsByTagName("ownedComment");
                	  if (comms.getLength() > 0) {
	                      String specs = ((Element)comms.item(0)).getElementsByTagName("body").item(0).getTextContent();
	                      String lines[] = specs.split("\\r?\\n");
	                      for (String el : lines) {
	                    	  String params[] = el.split("=");
	                    	  switch (params[0]) {
		                          case "multiplicity":  if (params[1].equals("1..n")) {
		                        	    multiplicity = 0;
		                          	} else {
		                          		multiplicity = Integer.parseInt(params[1]);
		                          	}
		                            break;
		                          case "schedulingFlag":  schedulingFlag = params[1];
		                            break;
		                          case "processorSpeed":  processorSpeed = params[1];
		                            break;
	                    	  }
	                      }
                	  }
                	  newProcessorNode(procName, multiplicity, schedulingFlag, processorSpeed);
                	  

                      NodeList nestedClassifiers = element.getElementsByTagName("nestedClassifier");
                      for (int i = 0; i < nestedClassifiers.getLength(); ++i) {
                    	  Task t = new Task();
                    	  t.proc = procName;
                    	  Element el = (Element)nestedClassifiers.item(i);
                    	  t.name = el.getAttribute("name");
                    	  t.multiplicity = 1;
                    	  NodeList task_comms = el.getElementsByTagName("ownedComment");
                    	  if (task_comms.getLength() > 0) {
    	                      String specs = ((Element)task_comms.item(0)).getElementsByTagName("body").item(0).getTextContent();
    	                      String lines[] = specs.split("\\r?\\n");
    	                      for (String line : lines) {
    	                    	  String params[] = line.split("=");
    	                    	  switch (params[0]) {
    		                          case "multiplicity":  if (params[1].equals("1..n")) {
    		                        	    t.multiplicity = 0;
    		                          	} else {
    		                          		t.multiplicity = Integer.parseInt(params[1]);
    		                          	}
    		                            break;
    	                    	  }
    	                      }
                    	  }
                    	  NodeList ownedAttributes = el.getElementsByTagName("ownedAttribute");
                    	  if (ownedAttributes.getLength() > 0) {
	                    	  class_prop_map.put(((Element)ownedAttributes.item(0)).getAttribute("xmi:id"), t.name);
	                    	  class_id_map.put(el.getAttribute("xmi:id"), t.name);
	                    	  tasks.add(t);
                    	  }
                      }
                  } else if (type.equals("uml:CommunicationPath")) {
                	  CommPath commPath = new CommPath();
                	  commPath.linkSpeed = "10000.0";
                	  
                	  NodeList comms = element.getElementsByTagName("ownedComment");
                	  if (comms.getLength() > 0) {
	                      String specs = ((Element)comms.item(0)).getElementsByTagName("body").item(0).getTextContent();
	                      String lines[] = specs.split("\\r?\\n");
	                      for (String el : lines) {
	                    	  String params[] = el.split("=");
	                    	  switch (params[0]) {
		                          case "LinkSpeed":  commPath.linkSpeed = params[1];
		                            break;
	                    	  }
	                      }
                	  }
                	  
                	  NodeList ownedEnds = element.getElementsByTagName("ownedEnd");
                	  if (ownedEnds.getLength() > 1) {
                		  commPath.proc1 = ((Element)ownedEnds.item(0)).getAttribute("name");
                		  commPath.proc2 = ((Element)ownedEnds.item(1)).getAttribute("name");
                		  commPaths.add(commPath);
                	  }
                  } else {

	                  /*System.out.println("Current Element :" + node.getNodeName());
	                  System.out.println("Node type : " + type);
	                  System.out.println("First Name : " + firstname);*/
                  }

              } else {
            	  //System.out.println("packedElement NodeType: " + node.getNodeType() );
              }
          }
        for (Task t : tasks) {
        	newTaskComponent(t.name, t.proc, t.multiplicity);
        }
        for (CommPath cp : commPaths) {
        	linkProcessorNodes(cp.proc1, cp.proc2, cp.linkSpeed);
        }
	}
	
	private void newProcessorNode(String processorId, int multiplicity, String schedulingFlag, String processorSpeed) {
		System.out.println("newProcessorNode: \nprocId=" + processorId + "\nmultiplicity=" + multiplicity + "\nschedulingFlag=" + schedulingFlag + "\nprocessorSpeed=" + processorSpeed);
	}
	
	private void newTaskComponent(String taskId, String processorId, int multiplicity) {
		System.out.println("newTaskComponent: \ntaskId=" + taskId + "\nprocessorId=" + processorId + "\nmultiplicity=" + multiplicity);
	}
	
	private void linkProcessorNodes(String proc1, String proc2, String linkSpeed) {
		System.out.println("linkProcessorNodes: \nproc1=" + proc1 + "\nproc2=" + proc2 + "\nlinkSpeed=" + linkSpeed);
	}
	
	private class ClientServerCollab {
		String client_id;
		String server_id;
		List<String> clients = new ArrayList<String>();
		List<String> servers = new ArrayList<String>();
	}
	
	private class CollabRole {
		String class_name;
		String role;
	}

	private void establishCollaborationInfo(Document doc) {
		NodeList list = doc.getElementsByTagName("packagedElement");
		
		List<ClientServerCollab> clientServerCollabs = new ArrayList<ClientServerCollab>();
		List<CollabRole> collabRoles = new ArrayList<CollabRole>();
		
		for (int temp = 0; temp < list.getLength(); temp++) {

            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;

                String type = element.getAttribute("xmi:type");
                
                if (type.equals("uml:Collaboration")) {
                	if (element.getAttribute("name").equals("CLIENT_SERVER")) {
	                	ClientServerCollab clientServerCollab = new ClientServerCollab();
	                	NodeList ownedAttributes = element.getElementsByTagName("ownedAttribute");
	                	for (int i = 0; i < ownedAttributes.getLength(); ++i) {
	                		String ownedAttributeName = ((Element)ownedAttributes.item(i)).getAttribute("name");
	                		if (ownedAttributeName.equals("Client")) {
	                			clientServerCollab.client_id = ((Element)ownedAttributes.item(i)).getAttribute("xmi:id");
	                		} else if (ownedAttributeName.equals("Server")) {
	                			clientServerCollab.server_id = ((Element)ownedAttributes.item(i)).getAttribute("xmi:id");
	                		}
	                	}
	                	if (!clientServerCollab.client_id.isEmpty() && !clientServerCollab.server_id.isEmpty()) {
	                		clientServerCollabs.add(clientServerCollab);
	                	}
                	}
                } else if (type.equals("uml:Dependency")) {
                	CollabRole collabRole = new CollabRole();
                	collabRole.class_name = class_id_map.get(element.getAttribute("supplier"));
                	collabRole.role = element.getAttribute("client");
                	if (collabRole.class_name != null) {
                		collabRoles.add(collabRole);
                	}
                }
            }
		}
		for (CollabRole cr : collabRoles) {
			boolean found = false;
			for (ClientServerCollab csc : clientServerCollabs) {
				if (cr.role.equals(csc.client_id)) {
					csc.clients.add(cr.class_name);
					found = true;
					break;
				}
				if (cr.role.equals(csc.server_id)) {
					csc.servers.add(cr.class_name);
					found = true;
					break;
				}
			}
			if (found) {
				continue;
			}
		}
		
		for (ClientServerCollab csc : clientServerCollabs) {
			for (String server : csc.servers) {
				for (String client : csc.clients) {
					addClientServerRelationship(client, server);
				}
			}
		}
	}
	
	private void addClientServerRelationship(String client, String server) {
		System.out.println("addClientServerRelationship: \nclient=" + client + "\nserver=" + server);
	}
	
	private class ActionSequence {
		String name;		
	}
	
	private class Instance {
		String instance_name;
		String task_name;
		boolean is_active;
	}
	
	private class Message {
		String name;
		String message;
		String send_id;
		String receive_id;
		String type;
		String from_inst;
		Element func;
	}
	
	private class Reply {
		String optArg;
		int argSize;
	}
	
	private class OperandContext {
		ActionSequence actionSequence;
		Element operand;
	}
	
	private int actionSequencesCount = 1;

	Map<String, Element> func_map = new HashMap<String, Element>();	
	
	private void createProblem(Document doc) {
		ActionSequence mainAS = createActionSequance("MainActionSequence");
		
		NodeList list = doc.getElementsByTagName("ownedOperation");
		//List<Message> messages = new ArrayList<Message>();
		
		for (int temp = 0; temp < list.getLength(); temp++) {

            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
            	Element element = (Element) node;
            	func_map.put(element.getAttribute("xmi:id"), element);
            }
		}
		
		list = doc.getElementsByTagName("packagedElement");
		
		for (int temp = 0; temp < list.getLength(); temp++) {

            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;
                
                if (element.getAttribute("xmi:type").equals("uml:Interaction") && element.getAttribute("name").equals("MainIntercation")) {
                	parseInteraction(element, mainAS);
                	return;
                }
            }
		}
	}
	
	private void parseInteraction(Element mainInteraction, ActionSequence mainAS) {
		NodeList list = mainInteraction.getElementsByTagName("lifeline");
		
		List<Instance> instances = new ArrayList<Instance>();

		Map<String, String> instance_map = new HashMap<String, String>();	
		
		String prevInst = "";
		
		for (int temp = 0; temp < list.getLength(); temp++) {

            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;

            	String taskName = class_prop_map.get(element.getAttribute("represents"));
            	instances.add(createNewInstance(element.getAttribute("name"), taskName, true));
            	instance_map.put(element.getAttribute("xmi:id"), element.getAttribute("name"));
            	if (prevInst.isEmpty()) {
            		prevInst = element.getAttribute("name");
            	}
            }
		}
		
		list = mainInteraction.getElementsByTagName("message");

		Map<String, Message> mes_map = new HashMap<String, Message>();	
		//List<Message> messages = new ArrayList<Message>();
		
		for (int temp = 0; temp < list.getLength(); temp++) {

            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;
                Message mes = new Message();
                mes.name = element.getAttribute("name");
                mes.message = element.getAttribute("xmi:id");
                mes.send_id = element.getAttribute("sendEvent");
                mes.receive_id = element.getAttribute("receiveEvent");
                mes.type = element.getAttribute("messageSort");
                mes.func = func_map.get(element.getAttribute("signature"));
                mes_map.put(element.getAttribute("xmi:id"), mes);
                /*Message mes = new Message();
                mes.id = element.getAttribute("xmi:id");
                mes.name = element.getAttribute("name");
                messages.add(mes);*/
            }
		}
		
		NodeList children = mainInteraction.getChildNodes();
		
		int timeVal = 100;
		
		timeVal = parseFragment(children, mes_map, instance_map, timeVal, prevInst, mainAS);
		
		for (Instance instance : instances) {
			terminateInstance(instance, timeVal / 100, mainAS, 0);
			timeVal += 100;
		}
	}
	
	private int parseFragment(NodeList children, Map<String, Message> mes_map, Map<String, String> instance_map, int timeVal, String prevInst, ActionSequence mainAS) {
		Map<String, Reply> reply_map = new HashMap<String, Reply>();	
		
		for (int temp = 0; temp < children.getLength(); temp++) {

            Node node = children.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;
                
                if (!element.getTagName().equals("fragment")) {
                	continue;
                }

                String type = element.getAttribute("xmi:type");
                
                if (type.equals("uml:MessageOccurrenceSpecification")) {
                	Message mes = mes_map.get(element.getAttribute("message"));
                	if (mes.name.equals("Sleep")) {
                		if (mes.send_id.equals(element.getAttribute("xmi:id"))) {
                			int sleepTime = 200;
                			sleep(instance_map.get(element.getAttribute("covered")), timeVal / 100, mainAS, sleepTime);
                			prevInst = instance_map.get(element.getAttribute("covered"));
                			timeVal += sleepTime;
                		}
                	} else if (mes.name.equals("Sleep_Rep")) {
                		continue;
                	} else if (mes.send_id.equals(element.getAttribute("xmi:id"))) {
                		mes.from_inst = element.getAttribute("covered");
                		mes_map.put(element.getAttribute("message"), mes);
                	} else if (mes.receive_id.equals(element.getAttribute("xmi:id"))) {
            			int argSize = 0;
            			String optArg = "";
            			int argSizeOut = 0;
            			String optArgOut = "";
            			int duration = 0;
            			if (mes.func != null) {
            				NodeList funcComms = mes.func.getElementsByTagName("ownedComment");
            				if (funcComms.getLength() > 0) {
      	                      String specs = ((Element)funcComms.item(0)).getElementsByTagName("body").item(0).getTextContent();
      	                      String lines[] = specs.split("\\r?\\n");
      	                      for (String el : lines) {
      	                    	  String params[] = el.split("=");
      	                    	  switch (params[0]) {
      		                          case "duration":  duration = Integer.parseInt(params[1]);
      		                            break;
      	                    	  }
      	                      }
                      	  }
            				NodeList ownedParameters = mes.func.getElementsByTagName("ownedParameter");
            				for (int i = 0; i < ownedParameters.getLength(); ++i) {
            					Element param = (Element)ownedParameters.item(i);
            					NodeList comms;
            					switch (param.getAttribute("direction")) {
		                          case "": if (optArg.isEmpty()) {
		                        	    optArg = param.getAttribute("name");
		                          	} else {
		                          		optArg = optArg + "&" + param.getAttribute("name");
		                          	}
			                          comms = param.getElementsByTagName("ownedComment");
			                    	  if (comms.getLength() > 0) {
			    	                      String specs = ((Element)comms.item(0)).getElementsByTagName("body").item(0).getTextContent();
			    	                      String lines[] = specs.split("\\r?\\n");
			    	                      for (String el : lines) {
			    	                    	  String params[] = el.split("=");
			    	                    	  switch (params[0]) {
			    		                          case "argSize":  argSize += Integer.parseInt(params[1]);
			    		                            break;
			    	                    	  }
			    	                      }
			                    	  }
		                            break;
		                          case "out": if (optArgOut.isEmpty()) {
		                        	    optArgOut = param.getAttribute("name");
		                          	} else {
		                          		optArgOut = optArgOut + "&" + param.getAttribute("name");
		                          	}
			                          comms = param.getElementsByTagName("ownedComment");
			                    	  if (comms.getLength() > 0) {
			    	                      String specs = ((Element)comms.item(0)).getElementsByTagName("body").item(0).getTextContent();
			    	                      String lines[] = specs.split("\\r?\\n");
			    	                      for (String el : lines) {
			    	                    	  String params[] = el.split("=");
			    	                    	  switch (params[0]) {
			    		                          case "argSize":  argSizeOut += Integer.parseInt(params[1]);
			    		                            break;
			    	                    	  }
			    	                      }
			                    	  }
		                            break;
            					}
            				}
            				Reply rep = new Reply();
            				rep.argSize = argSizeOut;
            				rep.optArg = optArgOut;
            				reply_map.put(mes.name + "_Rep", rep);
            			}

                		if (mes.type.isEmpty()) {
                			createSyncCall(instance_map.get(mes.from_inst), instance_map.get(element.getAttribute("covered")), mes.name, optArg, argSize, "", timeVal / 100, mainAS, 0);
                			prevInst = instance_map.get(element.getAttribute("covered"));
                			timeVal += 100;
                		} else if (mes.type.equals("reply")) {
                			Reply rep = reply_map.get(mes.name);
                			if (rep != null) {
                				argSize = rep.argSize;
                				optArg = rep.optArg;
                			}
                			createReplyCall(instance_map.get(mes.from_inst), instance_map.get(element.getAttribute("covered")), mes.name, optArg, argSize, "", timeVal / 100, mainAS, 0);
                			prevInst = instance_map.get(element.getAttribute("covered"));
                			timeVal += 100;
                		}
                		if (mes.func != null) {
                			createLocalAction(instance_map.get(element.getAttribute("covered")), mes.func.getAttribute("name"), "", timeVal / 100, mainAS, duration);
                			prevInst = instance_map.get(element.getAttribute("covered"));
                			timeVal += 100;
                			timeVal += duration;
                		}
                	}
                } else if (type.equals("uml:CombinedFragment")) {
                	NodeList operands = element.getChildNodes();
                	List<OperandContext> operandContexts = new ArrayList<OperandContext>();
                	for (int j = 0; j < operands.getLength(); ++j) {
                		if (operands.item(j).getNodeType() != Node.ELEMENT_NODE) {
                			continue;
                		}
                		Element operand = (Element) operands.item(j);
                		if (operand.getTagName().equals("operand")) {
                			OperandContext operandContext = new OperandContext();
                			
                			operandContext.operand = operand;
                			
                			NodeList guards = operand.getElementsByTagName("guard");
                			String optGuard = "";
                			
                			for (int k = 0; k < guards.getLength(); ++k) {
                				if (((Element)guards.item(k).getParentNode()).getAttribute("xmi:id").equals(operand.getAttribute("xmi:id"))) {
                					Element guardElement = (Element)guards.item(k);
                					String val = ((Element)guardElement.getElementsByTagName("specification").item(0)).getAttribute("value");
                					if (val.startsWith("p=")) {
                						optGuard = val.substring(2);
                					}
                					break;
                				}
                			}
                			
                			ActionSequence as = createActionSequence(prevInst, optGuard, timeVal / 100, mainAS, 0);
                			operandContext.actionSequence = as;
                			
                			operandContexts.add(operandContext);
                		}
                	}
                	for (OperandContext oc : operandContexts) {
                		parseFragment(oc.operand.getChildNodes(), mes_map, instance_map, 1, prevInst, oc.actionSequence);
                	}
                	timeVal += 100;
                }
            }
		}
		return timeVal;
	}
	
	private ActionSequence createActionSequance(String name) {
		ActionSequence actionSequence = new ActionSequence();
		actionSequence.name = name;
		return actionSequence;
	}
	
	private Instance createNewInstance(String instanceName, String taskName, boolean isActive) {
		Instance instance = new Instance();
		instance.instance_name = instanceName;
		instance.task_name = taskName;
		instance.is_active = isActive;
		System.out.println("Instance created:\ninstanceName=" + instanceName +"\ntaskName=" + taskName + "\nisActive=" + isActive);
		return instance;
	}
	
	private void sleep(String fromInstId, int timeVal, ActionSequence localAS, int expCycles) {
		System.out.println("sleep:\nfromInstId=" + fromInstId +"\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);		
	}
	
	private void createSyncCall(String fromInstId, String toInstId, String msgName, String optArg, int argSize, String optGuard, int timeVal, ActionSequence localAS, int expCycles) {
		System.out.println("createSyncCall:\nfromInstId=" + fromInstId + "\ntoInstId=" + toInstId + "\nmsgName=" + msgName + "\noptArg=" + optArg + "\nargSize=" + argSize + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);		
	}
	
	private void createReplyCall(String fromInstId, String toInstId, String msgName, String optArg, int argSize, String optGuard, int timeVal, ActionSequence localAS, int expCycles) {
		System.out.println("createReplyCall:\nfromInstId=" + fromInstId + "\ntoInstId=" + toInstId + "\nmsgName=" + msgName + "\noptArg=" + optArg + "\nargSize=" + argSize + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);		
	}
	
	private void createLocalAction(String instId, String actionName, String optGuard, int timeVal, ActionSequence localAS, int expCycles) {
		System.out.println("createLocalAction:\ninstId=" + instId + "\nactionName=" + actionName + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);		
	}
	
	private ActionSequence createActionSequence(String instId, String optGuard, int timeVal, ActionSequence localAS, int expCycles) {
		String actionName = "AS" + actionSequencesCount++;
		System.out.println("createActionSequence:\ninstId=" + instId + "\nactionName=" + actionName + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);
		ActionSequence actionSequence = new ActionSequence();
		actionSequence.name = actionName;
		return actionSequence;
	}
	
	private void terminateInstance(Instance theInst, int timeVal, ActionSequence localAS, int expCycles) {
		System.out.println("terminateInstance:\ntheInst=" + theInst.instance_name +"\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);	
	}
	
	private static final String FILENAME = "../h-orb/h-orb.uml";

	public static void main(String[] args) {
		MainClass prog = new MainClass();
		prog.run();
	}
	
	public void run() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(FILENAME));
			
			doc.getDocumentElement().normalize();
			System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
	        System.out.println("------");
	        configurePlatform(doc);
	        establishCollaborationInfo(doc);
	        createProblem(doc);
			System.out.println("hello kp0hyc");
		} catch (ParserConfigurationException | SAXException | IOException e) {
	          e.printStackTrace();
	    }
	}

}
