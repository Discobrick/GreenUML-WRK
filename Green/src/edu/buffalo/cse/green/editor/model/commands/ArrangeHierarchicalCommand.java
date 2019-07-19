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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.emf.common.util.Reflect;
import org.eclipse.gef.commands.Command;
import org.eclipse.jdt.core.JavaModelException;

import ccvisu.GraphData;
import ccvisu.GraphEdge;
import ccvisu.Minimizer;
import ccvisu.MinimizerBarnesHut;
import ccvisu.Options;
import edu.buffalo.cse.green.ccvisu.CCVisuUtil;
import edu.buffalo.cse.green.ccvisu.GraphVertex;
import edu.buffalo.cse.green.editor.DiagramEditor;
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
public class ArrangeHierarchicalCommand extends Command {

	private int[][] opos;
	private int[][] npos;
	private Vector<TypeModel> _m;
	
	/**
	 * 
	 */
	public ArrangeHierarchicalCommand() {
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
		
	// A more simple hierarchical model
	public void execute() {
	        DiagramEditor editor = DiagramEditor.getActiveEditor();
	        RootModel root = editor.getRootModel();
	        List<RelationshipModel> rels = root.getRelationships();	 
	        List<AbstractModel> mods = root.getChildren();
	        int maxLevel = getMaxLevel(mods);
	        int[] posX = new int[maxLevel+1];
	        
	       	    	        
	        System.out.println("MaxLevel: " + maxLevel);
	        
	        for (AbstractModel m : mods) {
	            if (m instanceof TypeModel) {
	            	int lvl = getLevel((TypeModel)m);
	            	int level = maxLevel - lvl;
	            	System.out.println(m.getClass().getSimpleName() + " level: " + level);
	            	m.setSize(200, 100);
	            	m.setLocation(posX[level], (level * (-150)));
	            	posX[level] += 300;
	            }
	        }
	 

	        editor.checkDirty();
	
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
