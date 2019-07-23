/* This file is part of Green.
 *
 * Copyright (C) 2005 The Research Foundation of State University of New York
 * All Rights Under Copyright Reserved, The Research Foundation of S.U.N.Y.
 * 
 * Green is free software, licensed under the terms of the Eclipse
 * Public License, version 1.0.  The license is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * 
 */
package edu.buffalo.cse.green.editor.model.commands;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.common.util.Reflect;
import org.eclipse.gef.commands.Command;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import ccvisu.GraphData;
import ccvisu.GraphEdge;
import ccvisu.Minimizer;
import ccvisu.MinimizerBarnesHut;
import ccvisu.Options;
import edu.buffalo.cse.green.ccvisu.CCVisuUtil;
import edu.buffalo.cse.green.ccvisu.GraphVertex;
import edu.buffalo.cse.green.editor.DiagramEditor;
import edu.buffalo.cse.green.editor.action.ZoomFitAction;
import edu.buffalo.cse.green.editor.model.AbstractModel;
import edu.buffalo.cse.green.editor.model.CompartmentModel;
import edu.buffalo.cse.green.editor.model.FieldModel;
import edu.buffalo.cse.green.editor.model.MethodModel;
import edu.buffalo.cse.green.editor.model.RelationshipModel;
import edu.buffalo.cse.green.editor.model.RootModel;
import edu.buffalo.cse.green.editor.model.TypeModel;
import sun.reflect.Reflection;

/**
 * @author zgwang
 *
 */
public class AutoArrangeCommand extends Command {

	private int[][] opos;
	private int[][] npos;
	private Vector<TypeModel> _m;
	
	/**
	 * 
	 */
	public AutoArrangeCommand() {
		_m = new Vector<TypeModel>();
	}
	
	public void undo() {
		for( int i=0; i<_m.size(); i++)
			_m.get(i).setLocation(opos[i][0], opos[i][1]);
	}
	
	public void redo() {
		for( int i=0; i<_m.size(); i++)
			_m.get(i).setLocation(npos[i][0], npos[i][1]);
	}
	
	
	// Optimized layout for more complicated graphs
	public void execute2() {
	        DiagramEditor editor = DiagramEditor.getActiveEditor();
	        RootModel root = editor.getRootModel();
	        List<RelationshipModel> rels = root.getRelationships();
	 
	        List<AbstractModel> mods = root.getChildren();

	        GraphData gd = new GraphData();
	        for (AbstractModel m : mods) {

	            if (m instanceof TypeModel) {
	 
	                final TypeModel n = (TypeModel) m;
	 
	                GraphVertex me = new GraphVertex(n);
	                me.id = gd.vertices.size();
	                me.name = "" + me.id;
	                Dimension size = editor.getRootPart().getPartFromModel(n).getFigure().getSize();
	                me.degree = n.getIncomingEdges().size() + n.getOutgoingEdges().size()
	                        + (size.height * size.width / 20000.0f);
	                me.isSource = me.degree > 0;
	                me.pos.x = n.getLocation().x / 200.0f;
	                me.pos.y = n.getLocation().y / 200.0f;
	                if (!gd.vertices.contains(me)) {
	                    gd.vertices.add(me);
	                    _m.add(n);
	                } else
	                    me = (GraphVertex) gd.vertices.get(gd.vertices.indexOf(me));

	                for (RelationshipModel e : n.getOutgoingEdges()) {                   
	                   
	                    TypeModel y = e.getTargetModel();
	                   
	                    if (n.equals(y))
	                        continue; // no reflexive graph
	                    GraphVertex you = new GraphVertex(y);
	                    you.id = gd.vertices.size();
	                    you.name = "" + you.id;
	                    you.degree = y.getIncomingEdges().size()
	                            + y.getOutgoingEdges().size() /* (y.getSize().height + y.getSize().width) */;
	                    you.isSource = you.degree > 0;
	                    you.pos.x = y.getLocation().x / 200.0f;
	                    you.pos.y = y.getLocation().y / 200.0f;
	                    if (!gd.vertices.contains(you)) {
	                        gd.vertices.add(you);
	                        _m.add(y);
	                    } else
	                        you = (GraphVertex) gd.vertices.get(gd.vertices.indexOf(you));
	 
	                    // have me, you
	                    // create edge
	 
	                    if (me.id != you.id) {
	                        GraphEdge ed = new GraphEdge();
	                        ed.x = me.id;
	                        ed.y = you.id;
	                        ed.w = 1.0f;
	                        gd.edges.add(ed);
	                    }
	                    
	                }
	            }
	        }
	 
	        Options options = CCVisuUtil.newOptions(gd, 100, 3, 1, false, false, 2.001f, null, false);
	        Minimizer me = new MinimizerBarnesHut(options);

	 
	        me.minimizeEnergy();
	 
	        // normalize
	        float lx = 999, ly = 999;
	        for (ccvisu.GraphVertex v : gd.vertices) {
	            if (v.pos.x < lx)
	                lx = v.pos.x;
	            if (v.pos.y < ly)
	                ly = v.pos.y;
	        }
	 
	        for (ccvisu.GraphVertex v : gd.vertices) {
	            v.pos.x -= lx;
	            v.pos.y -= ly;
	        }
	 
	        opos = new int[_m.size()][2];
	        npos = new int[_m.size()][2];
	 
	        for (int i = 0; i < gd.vertices.size(); i++) {
	            opos[i][0] = _m.get(i).getLocation().x;
	            opos[i][1] = _m.get(i).getLocation().y;
	            GraphVertex v = (GraphVertex) gd.vertices.get(i);
	            v.me.setLocation((int) (v.pos.x * 200), (int) (v.pos.y * 200));
	            npos[i][0] = v.me.getLocation().x;
	            npos[i][1] = v.me.getLocation().y;
	        }

	        editor.checkDirty();
	    }
	
	// A more simple hierarchical model
	// Actually works
	public void execute3() {
	        DiagramEditor editor = DiagramEditor.getActiveEditor();
	        RootModel root = editor.getRootModel();
	        List<RelationshipModel> rels = root.getRelationships();	 
	        List<AbstractModel> mods = root.getChildren();
	        int maxLevel = getMaxLevel(mods);
	        int[] posX = new int[maxLevel+1];
	        
	        for (AbstractModel m : mods) {
	            if (m instanceof TypeModel) {
	            	int lvl = getLevel((TypeModel)m);
	            	int level = maxLevel - lvl;

	            	m.setSize(200, 100);
	            	m.setLocation(posX[level], (level * (-150)));
	            	posX[level] += 300;
	            }
	        }
	 

	        editor.checkDirty();
	
	}
	
	public void execute() {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
        RootModel root = editor.getRootModel();
		List<AbstractModel> children = root.getChildren();

		int maxLevel = getMaxLevel(children);
		List<TypeModel> tops = new ArrayList<>();
		int nextX = 0;
		List<String> classes = new ArrayList<>();
		
		for(AbstractModel mod : children) {
			if(mod instanceof TypeModel) {
				try {
					if((((TypeModel) mod).getType().getSuperclassName()) == null) {
						tops.add((TypeModel) mod);
					}
				} catch (JavaModelException e) {
				}
			}
		}
		
		
		for(AbstractModel t : tops) {
			if(t instanceof TypeModel) {

				nextX = drawTree8((TypeModel) t, maxLevel, nextX);
			}
		}
		
		editor.checkDirty();
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_A);
	        robot.keyRelease(KeyEvent.VK_A);
	        
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public int drawTree8(TypeModel top, int max, int prevX) {
        DiagramEditor editor = DiagramEditor.getActiveEditor();
        List<AbstractModel> allModels = getAllModels(top);
        int[] xPos = new int[max + 1];
        Arrays.fill(xPos, (prevX + 100));
        int nextX = 0;
           	    	        

        
        for (AbstractModel m : allModels) {
            if (m instanceof TypeModel) {
            	int lvl = getLevel((TypeModel)m);
            	int level = max - lvl;

            	m.setSize(200, 100);
            	m.setLocation(xPos[level], (level * (-150)));
            	xPos[level] += 300;
            }
        }
        
        for(int i = 0; i < xPos.length; i++) {
        	if(xPos[i] > nextX)
        		nextX = xPos[i];
        }
 

        editor.checkDirty();
        return nextX;

}
	
	public List<AbstractModel> getAllModels(TypeModel top) {
		List<AbstractModel> c = new ArrayList<>();
		Set<RelationshipModel> set = top.getIncomingEdges();

		int repeat = set.size();
		
		if(repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext(); ) {
			       RelationshipModel f = it.next();
			       List<AbstractModel> temp = (getAllModels(f.getSourceModel()));
			       	for(AbstractModel m : temp) {
			       		c.add(m);
			       	}
			   }
			 
		}
		else {}
		c.add(top);
		return c;
	}
	
	// Finds the maximum depth out of a list of TypeModels
	public int getMaxLevel(List<AbstractModel> list) {
		int maxLevel = 0;
		
		for(AbstractModel mod : list) {
			if(mod instanceof TypeModel) {
				int lvl = getLevel((TypeModel) mod);
				if(lvl > maxLevel) {
					maxLevel = lvl;
				}
			}
		}
		
		return maxLevel;
	}
	
	// Finds the depth for a single TypeModel
	public int getLevel(TypeModel mod) {
		
		int maxLevel = 0;
		Set<RelationshipModel> set = mod.getOutgoingEdges();
		int repeat = set.size();
		
		if(repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext(); ) {
			       RelationshipModel f = it.next();
			       	int lvl = getLevel(f.getTargetModel()) +1;
			       	if(lvl > maxLevel) {
			       		maxLevel = lvl;
			       	}
			   }
		}
		else {
			return 0;
			}
		return maxLevel;
	}
	
	
	
	
}
