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
import java.util.Arrays;
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
public class ArrangeTreeCommand extends Command {

	private int[][] opos;
	private int[][] npos;
	private Vector<TypeModel> _m;

	/**
	 * 
	 */
	public ArrangeTreeCommand() {
		_m = new Vector<TypeModel>();
	}

	public void undo() {
		for (int i = 0; i < _m.size(); i++)
			_m.get(i).setLocation(opos[i][0], opos[i][1]);
	}

	public void redo() {
		for (int i = 0; i < _m.size(); i++)
			_m.get(i).setLocation(npos[i][0], npos[i][1]);
	}

	// A more simple Tree model
	public void execute() {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		RootModel root = editor.getRootModel();
		List<AbstractModel> children = root.getChildren();
		System.out.println("Children: " + children);
		int maxLevel = getMaxLevel(children);
		List<TypeModel> tops = new ArrayList<>();
		int nextX = 0;
		List<String> classes = new ArrayList<>();

		System.out.println("Execute");

		for (AbstractModel mod : children) {
			if (mod instanceof TypeModel) {
				try {
					if ((((TypeModel) mod).getType().getSuperclassName()) == null) {
						tops.add((TypeModel) mod);
					}
				} catch (JavaModelException e) {
				}
			}
		}

		System.out.println(tops);

		for (AbstractModel t : tops) {
			if (t instanceof TypeModel) {
				System.out.println("New tree");
				nextX = drawTree8((TypeModel) t, maxLevel, nextX);
			}
		}

		editor.checkDirty();
		
		
	}

	public int drawTree8(TypeModel top, int max, int prevX) {
		DiagramEditor editor = DiagramEditor.getActiveEditor();
		List<AbstractModel> allModels = getAllModels(top);
		//System.out.println("AllModels: " + allModels);
		int[] xPos = new int[max + 1];
		Arrays.fill(xPos, (prevX + 100));
		int nextX = 0;

		System.out.println("MaxLevel: " + max);

		for (AbstractModel m : allModels) {
			if (m instanceof TypeModel) {
				int lvl = getLevel((TypeModel) m);
				int level = max - lvl;
				System.out.println(m.getClass().getSimpleName() + " level: " + level + "Incoming: "
						+ ((TypeModel) m).getIncomingEdges().size());
				m.setSize(200, 100);
				m.setLocation(xPos[level], (level * (-150)));
				xPos[level] += 100;
			}
		}

		for (int i = 0; i < xPos.length; i++) {
			if (xPos[i] > nextX)
				nextX = xPos[i];
		}
		
		editor.checkDirty();
		return nextX;


	}

	public List<AbstractModel> getAllModels(TypeModel top) {
		List<AbstractModel> c = new ArrayList<>();
		Set<RelationshipModel> set = top.getIncomingEdges();
		System.out.println("Outgoing: " + set);
		int repeat = set.size();

		if (repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext();) {
				RelationshipModel f = it.next();
				List<AbstractModel> temp = (getAllModels(f.getSourceModel()));
				for (AbstractModel m : temp) {
					c.add(m);
				}
			}

		} else {
		}
		c.add(top);
		return c;
	}

	// Finds the maximum depth out of a list of TypeModels
	public int getMaxLevel(List<AbstractModel> list) {
		int maxLevel = 0;

		for (AbstractModel mod : list) {
			if (mod instanceof TypeModel) {
				int lvl = getLevel((TypeModel) mod);
				if (lvl > maxLevel) {
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

		if (repeat != 0) {
			for (Iterator<RelationshipModel> it = set.iterator(); it.hasNext();) {
				RelationshipModel f = it.next();
				int lvl = getLevel(f.getTargetModel()) + 1;
				if (lvl > maxLevel) {
					maxLevel = lvl;
				}
			}
		} else {
			return 0;
		}
		return maxLevel;
	}

}
