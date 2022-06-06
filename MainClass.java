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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainClass {
	private Map<String, String> class_id_map = new HashMap<String, String>();
	private Map<String, String> class_prop_map = new HashMap<String, String>();
	
	List<Processor> processors = new ArrayList<Processor>();
	Map<String, Task> tasks = new HashMap<String, Task>();
	Map<String, HashMap<String, EntryLink>> link_callers_map = new HashMap<String, HashMap<String, EntryLink>>();
	
	private class Task {
		public String name;
		public String proc;
		public int multiplicity;
		public String role;
		public int entries = 0;
	}
	
	private class Entry {
		public boolean started;
		public boolean initiator;
		public String task;
		public String msg;
		public int phase_1_time;
		public int phase_2_time;
		public int phase_3_time;
		public int phase_1_type;
		public int phase_2_type;
		public int phase_3_type;
	}
	
	public Map<String, Entry> entries = new HashMap<String, Entry>();
	
	//ToDo: delete name
	public String addEntry(String _delete_it, String task, String msg, boolean started, boolean initiator) {
		Entry e = new Entry();
		e.initiator = initiator;
		e.started = started;
		e.task = task;
		e.phase_1_time = 0;
		e.phase_2_time = 0;
		e.phase_3_time = 0;
		e.phase_1_type = 0;
		e.phase_2_type = 0;
		e.phase_3_type = 0;
		e.msg = msg;
		Task t = tasks.get(task);
		t.entries++;
		String entry_name = task + "_" + msg;
		entries.put(entry_name, e);
		tasks.put(task, t);
		return entry_name;
	}
	
	//ToDo: delete name
	public String addDefaultEntry(String _delete_it, String task, boolean started, boolean initiator) {
		Entry e = new Entry();
		e.initiator = initiator;
		e.started = started;
		e.task = task;
		e.phase_1_time = 0;
		e.phase_2_time = 0;
		e.phase_3_time = 0;
		e.phase_1_type = 0;
		e.phase_2_type = 0;
		e.phase_3_type = 0;
		String entry_name = task + "_e0";
		entries.put(entry_name, e);
		return entry_name;
	}
	
	public void addEntryLink(String from, String to, float count) {
		if(!link_callers_map.containsKey(from)) {
			link_callers_map.put(from, new HashMap<String, EntryLink>());
		}
		
		HashMap<String, EntryLink> instEntryLinks = link_callers_map.get(from);
		
		EntryLink currentLink = null;
		
		if (!instEntryLinks.containsKey(to)) {
			currentLink = new EntryLink();
			currentLink.caller = from;
			currentLink.reciever = to;
			currentLink.phase_1_calls = 0;
			currentLink.phase_2_calls = 0;
			currentLink.phase_3_calls = 0;
		} else {
			currentLink = instEntryLinks.get(to);
		}
		currentLink.phase_1_calls += count;
		instEntryLinks.put(to, currentLink);
		link_callers_map.put(from, instEntryLinks);
	}
	
	public String getTaskDefaultEntry(String task) {
		return task + "_e0";
	}
	
	public boolean entryExists(String inst, String msg) {
		return entries.containsKey(inst + "_" + msg);
	}
	
	private class EntryLink {
		public String caller;
		public String reciever;
		public float phase_1_calls;
		public float phase_2_calls;
		public float phase_3_calls;		
	}

	private class CommPath {
		public String proc1;
		public String proc2;
		public int linkSpeed;
	}
	
	private class Processor {
		String processorId;
		int multiplicity;
		String schedulingFlag;
		int processorSpeed;
	}
	
	List<CommPath> commPaths = new ArrayList<CommPath>();
	
	private int getLinkSpeed(String proc1, String proc2) {
		for (CommPath cp : commPaths) {
			if ((cp.proc1.equals(proc1) && cp.proc2.equals(proc2)) || (cp.proc1.equals(proc2) && cp.proc2.equals(proc1))) {
				return cp.linkSpeed;
			}
		}
		//Default value
		return 1000;
	}
	
	private int getProcessorSpeed(String proc) {
		for (Processor p : processors) {
			if (p.processorId.equals(proc)) {
				return p.processorSpeed;
			}
		}
		//default value
		return 1;
	}
	
	private void configurePlatform(Document doc) {
		NodeList list = doc.getElementsByTagName("packagedElement");
		
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
	                    	  tasks.put(t.name, t);
                    	  }
                      }
                  } else if (type.equals("uml:CommunicationPath")) {
                	  CommPath commPath = new CommPath();
                	  commPath.linkSpeed = 1000;
                	  
                	  NodeList comms = element.getElementsByTagName("ownedComment");
                	  if (comms.getLength() > 0) {
	                      String specs = ((Element)comms.item(0)).getElementsByTagName("body").item(0).getTextContent();
	                      String lines[] = specs.split("\\r?\\n");
	                      for (String el : lines) {
	                    	  String params[] = el.split("=");
	                    	  switch (params[0]) {
		                          case "LinkSpeed":  commPath.linkSpeed = Math.round(Float.parseFloat(params[1]));
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

        Task task = new Task();
        task.name = "Sleep_T";
        task.proc = "Sleep_P";
        task.multiplicity = 0;
        tasks.put(task.name, task);
  	    newProcessorNode("Sleep_p", 0, "f", "1.0");
        for (Task t : tasks.values()) {
        	newTaskComponent(t.name, t.proc, t.multiplicity);
        }
        for (CommPath cp : commPaths) {
        	linkProcessorNodes(cp.proc1, cp.proc2, cp.linkSpeed);
        }
	}
	
	private void newProcessorNode(String processorId, int multiplicity, String schedulingFlag, String processorSpeed) {
		System.out.println("newProcessorNode: \nprocId=" + processorId + "\nmultiplicity=" + multiplicity + "\nschedulingFlag=" + schedulingFlag + "\nprocessorSpeed=" + processorSpeed);
		Processor p = new Processor();
		p.processorId = processorId;
		p.multiplicity = multiplicity;
		p.schedulingFlag = schedulingFlag;
		p.processorSpeed = Math.round(Float.valueOf(processorSpeed));
		processors.add(p);
	}
	
	private void newTaskComponent(String taskId, String processorId, int multiplicity) {
		System.out.println("newTaskComponent: \ntaskId=" + taskId + "\nprocessorId=" + processorId + "\nmultiplicity=" + multiplicity);
	}
	
	private void linkProcessorNodes(String proc1, String proc2, int linkSpeed) {
		System.out.println("linkProcessorNodes: \nproc1=" + proc1 + "\nproc2=" + proc2 + "\nlinkSpeed=" + String.valueOf(linkSpeed));
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
					Task t = tasks.get(cr.class_name);
					t.role = "r";
					//t.addEntry("reference_type", true, true); Do we need it?
					tasks.put(cr.class_name, t);
					found = true;
					break;
				}
				if (cr.role.equals(csc.server_id)) {
					csc.servers.add(cr.class_name);
					Task t = tasks.get(cr.class_name);
					t.role = "n";
					tasks.put(cr.class_name, t);
					found = true;
					break;
				}
			}
			if (found) {
				continue;
			}
		}
		
		Task t = tasks.get("Sleep_T");
		t.role = "n";
		tasks.put("Sleep_T", t);
		
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
		float calls;
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
	
	private void createProblem(Document doc)  throws Exception {
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
	
	private void parseInteraction(Element mainInteraction, ActionSequence mainAS)  throws Exception {
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

		HashMap<String, String> current_entry = new HashMap<>();
		HashMap<String, Boolean> resolve_entry = new HashMap<>();
		timeVal = parseFragment(children, mes_map, instance_map, timeVal, prevInst, instances, mainAS, current_entry, resolve_entry);
		
		for (Instance instance : instances) {
			terminateInstance(instance, timeVal / 100, mainAS, 0);
			timeVal += 100;
		}
	}
	
	private int parseFragment(NodeList children, Map<String, Message> mes_map, Map<String, String> instance_map, int timeVal, String prevInst, List<Instance> instances, ActionSequence mainAS, HashMap<String, String> current_entry, HashMap<String, Boolean> resolve_entry) throws Exception  {
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
            			//if it is local action?
                		if (mes.type.isEmpty()) {
                			createSyncCall(instance_map.get(mes.from_inst), instance_map.get(element.getAttribute("covered")), mes.name, optArg, argSize, "", timeVal / 100, mainAS, duration, current_entry, resolve_entry);
                			prevInst = instance_map.get(element.getAttribute("covered"));
                			if (duration == 0) {
                				timeVal += 100;
                			} else {
                				timeVal += duration;                				
                			}
                		} else if (mes.type.equals("reply")) {
                			Reply rep = reply_map.get(mes.name);
                			if (rep != null) {
                				//Is this arguments correct if it is not null?
                				argSize = rep.argSize;
                				optArg = rep.optArg;
                			}
                			createReplyCall(instance_map.get(mes.from_inst), instance_map.get(element.getAttribute("covered")), mes.name, optArg, argSize, "", timeVal / 100, mainAS, duration, current_entry, resolve_entry);
                			prevInst = instance_map.get(element.getAttribute("covered"));
                			if (duration == 0) {
                				timeVal += 100;
                			} else {
                				timeVal += duration;                				
                			}
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
                		HashMap<String, String> entry_copies = new HashMap<String, String>(current_entry);
                		parseFragment(oc.operand.getChildNodes(), mes_map, instance_map, 1, prevInst, instances, oc.actionSequence, entry_copies, resolve_entry);
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
		actionSequence.calls = 1;
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
	
	private void createSyncCall(String fromInstId, String toInstId, String msgName, String optArg, float argSize, String optGuard, int timeVal, ActionSequence localAS, int expCycles, Map<String, String> current_entry,  Map<String, Boolean> resolve_entry) throws Exception {
		if (fromInstId.equals(toInstId)) {
			createLocalAction(fromInstId, msgName, optGuard, timeVal, localAS, expCycles, current_entry, resolve_entry);
			return;
		}
		System.out.println("createSyncCall:\nfromInstId=" + fromInstId + "\ntoInstId=" + toInstId + "\nmsgName=" + msgName + "\noptArg=" + optArg + "\nargSize=" + argSize + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);	
		
		if (resolve_entry.containsKey(toInstId) && !resolve_entry.get(toInstId)) {
			throw new Exception("Entered resolved entry");
		}
		
		if (current_entry.containsKey(toInstId)) {
			throw new Exception("Entry was not closed and tried to re-open");
		}
		
		if (resolve_entry.containsKey(fromInstId) && !resolve_entry.get(fromInstId)) {
			resolve_entry.put(toInstId, false);
			System.out.println("Already resolved");
			return;
		}
		
		if (entryExists(toInstId, msgName)) {
			resolve_entry.put(toInstId, false);
			System.out.println("Entered to resolved entry");
			return;
		}
			
		Task t1 = tasks.get(fromInstId);
		Task t2 = tasks.get(toInstId);
		
		int linkSpeed = getLinkSpeed(t1.proc, t2.proc);
		int procSpeed = getProcessorSpeed(t2.proc);
		
		String from_entry = "";
		
		if (!current_entry.containsKey(fromInstId)) {
			if (!entries.containsKey(getTaskDefaultEntry(fromInstId))) {
				from_entry = addDefaultEntry("", fromInstId, true, true);
				current_entry.put(fromInstId, from_entry);
			} else {
				from_entry = getTaskDefaultEntry(fromInstId);
			}
		} else {
			from_entry = current_entry.get(fromInstId);
		}
		
		String to_entry = addEntry("", toInstId, msgName, true, false);
		current_entry.put(toInstId, to_entry);
		
		Entry to_entry_obj = entries.get(to_entry);
		float time = 0;
		if (linkSpeed != 0) {
			time += argSize/linkSpeed;
		}
		if (procSpeed != 0) {
			time += expCycles/procSpeed;
		}
		to_entry_obj.phase_1_time += time;
		entries.put(to_entry, to_entry_obj);
		
		float callNums = localAS.calls;
		localAS.calls = 1;
		
		addEntryLink(from_entry, to_entry, callNums);
	}
	
	private void createReplyCall(String fromInstId, String toInstId, String msgName, String optArg, int argSize, String optGuard, int timeVal, ActionSequence localAS, int expCycles, Map<String, String> current_entry,  Map<String, Boolean> resolve_entry) throws Exception {
		if (fromInstId.equals(toInstId)) {
			createLocalAction(fromInstId, msgName, optGuard, timeVal, localAS, expCycles, current_entry, resolve_entry);
			return;
		}
		System.out.println("createReplyCall:\nfromInstId=" + fromInstId + "\ntoInstId=" + toInstId + "\nmsgName=" + msgName + "\noptArg=" + optArg + "\nargSize=" + argSize + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);	
		
		if (resolve_entry.containsKey(toInstId) && !resolve_entry.get(toInstId) && (!resolve_entry.containsKey(fromInstId) || resolve_entry.get(fromInstId))) {
			throw new Exception("Reply from unresolved to resolved entry");
		}
		
		if (!current_entry.containsKey(fromInstId)) {
			throw new Exception("No entries on replying instance");
		}
		
		if (!current_entry.containsKey(toInstId)) {
			throw new Exception("No entries on replied instance");
		}
		
		if (!entryExists(fromInstId, msgName.substring(0,msgName.length() - 4))) {
			throw new Exception("No entry to reply");
		}
		
		if (!current_entry.get(fromInstId).equals(fromInstId + "_" + msgName.substring(0,msgName.length() - 4))) {
			throw new Exception("Incorrect reply message");			
		}
		
		if (!resolve_entry.containsKey(fromInstId)) {
			resolve_entry.put(fromInstId, true);
			System.out.println("Exit resolved entry");
			return;
		}
			
		Task t1 = tasks.get(fromInstId);
		Task t2 = tasks.get(toInstId);
		
		int linkSpeed = getLinkSpeed(t1.proc, t2.proc);
		int procSpeed = getProcessorSpeed(t2.proc);
		
		String from_entry = current_entry.get(fromInstId);
		Entry from_entry_obj = entries.get(from_entry);
		float time = 0;
		if (linkSpeed != 0) {
			time += argSize/linkSpeed;
		}
		if (procSpeed != 0) {
			time += expCycles/procSpeed;
		}
		from_entry_obj.phase_1_time += time;
		entries.put(from_entry, from_entry_obj);
		current_entry.remove(fromInstId);
	}
	
	private void createLocalAction(String instId, String actionName, String optGuard, int timeVal, ActionSequence localAS, int expCycles, Map<String, String> current_entry,  Map<String, Boolean> resolve_entry) throws Exception{
		System.out.println("createLocalAction:\ninstId=" + instId + "\nactionName=" + actionName + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);		
		
		if (resolve_entry.containsKey(instId) && !resolve_entry.get(instId)) {
			resolve_entry.put(instId, false);
			System.out.println("Already resolved");
			return;
		}
		
		if (!current_entry.containsKey(instId)) {
			if (!entries.containsKey(getTaskDefaultEntry(instId))) {
				String this_entry = addDefaultEntry("", instId, true, true);
				current_entry.put(instId, this_entry);
			} else {
				throw new Exception("Action without entry");
			}
		}
		
		String this_entry = current_entry.get(instId);

		Task t = tasks.get(instId);
		int procSpeed = getProcessorSpeed(t.proc);
		
		Entry this_entry_obj = entries.get(this_entry);
		float time = 0;
		if (procSpeed != 0) {
			time += expCycles/procSpeed;
		}
		this_entry_obj.phase_1_time += time;
		entries.put(this_entry, this_entry_obj);
	}
	
	private ActionSequence createActionSequence(String instId, String optGuard, int timeVal, ActionSequence localAS, int expCycles) {
		String actionName = "AS" + actionSequencesCount++;
		System.out.println("createActionSequence:\ninstId=" + instId + "\nactionName=" + actionName + "\noptGuard=" + optGuard + "\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);
		ActionSequence actionSequence = new ActionSequence();
		actionSequence.name = actionName;
		if (!optGuard.isEmpty()) {
			actionSequence.calls = Float.valueOf(optGuard);
		}
		return actionSequence;
	}
	
	private void terminateInstance(Instance theInst, int timeVal, ActionSequence localAS, int expCycles) {
		System.out.println("terminateInstance:\ntheInst=" + theInst.instance_name +"\ntimeVal=" + timeVal + "\nlocalAS=" + localAS.name + "\nexpCycles=" + expCycles);	
	}
	
	Map<String, ArrayList<String>> sortedEntries = new HashMap<String, ArrayList<String>>();
	
	private void sortEntries() {
		for (Map.Entry<String, Entry> e : entries.entrySet()) {
			String task_name = e.getValue().task;
			ArrayList<String> local_entries = new ArrayList<String>();
			if (sortedEntries.containsKey(task_name)) {
				local_entries = sortedEntries.get(task_name);
			}
			local_entries.add(e.getKey());
			sortedEntries.put(task_name, local_entries);
		}
	}
	
	private void createLQN() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(OUTFILE));
		writer.write("G \"...\" 0.00001 100 1 0.9 -1\n\n");
		writer.write("P  " + processors.size() + "\n");
		for (Processor p : processors) {
			writer.write("P " + p.processorId + " " + p.schedulingFlag);
			if (p.multiplicity == 0) {
				writer.write(" i\n");
			} else {
				writer.write(" m " + p.multiplicity + "\n");				
			}
		}
		writer.write("-1\n\n");
		
		addDefaultEntry("", "Sleep_T", false, false);
		writer.write("T  " + tasks.size() + "\n");
		sortEntries();
		for (Task t : tasks.values()) {
			writer.write("T " + t.name + " " + t.role);
			
			for (String entry_name : sortedEntries.get(t.name)) {
				writer.write(" " + entry_name);
			}
			writer.write(" -1 " + t.proc);
			if (t.multiplicity == 0) {
				writer.write(" i\n");
			} else {
				writer.write(" m " + t.multiplicity + "\n");				
			}
		}
		writer.write("-1\n\n");
		
		
		writer.close();
	}
	
	private static final String FILENAME = "../h-orb/h-orb.uml";
	private static final String OUTFILE = "solution.lqn";

	public static void main(String[] args)  throws Exception {
		MainClass prog = new MainClass();
		prog.run();
	}
	
	public void run()  throws Exception {
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
	        createLQN();
			System.out.println("hello kp0hyc");
		} catch (ParserConfigurationException | SAXException | IOException e) {
	          e.printStackTrace();
	    }
	}

}
