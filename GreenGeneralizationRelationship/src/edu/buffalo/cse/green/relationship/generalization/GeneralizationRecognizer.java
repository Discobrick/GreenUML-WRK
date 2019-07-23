/* This file is part of Green.
 *
 * Copyright (C) 2005 The Research Foundation of State University of New York
 * All Rights Under Copyright Reserved, The Research Foundation of S.U.N.Y.
 * 
 * Green is free software, licensed under the terms of the Eclipse
 * Public License, version 1.0.  The license is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package edu.buffalo.cse.green.relationship.generalization;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

import edu.buffalo.cse.green.editor.model.RelationshipKind;
import edu.buffalo.cse.green.relationships.RelationshipRecognizer;
import edu.buffalo.cse.green.relationships.DeclarationInfoProvider;

/**
 * @see edu.buffalo.cse.green.relationship.RelationshipRecognizer
 * 
 * @author bcmartin
 */
public class GeneralizationRecognizer extends RelationshipRecognizer {
	/**
	 * @see edu.buffalo.cse.green.relationships.RelationshipVisitor#process(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	public boolean process(DeclarationInfoProvider node) {
		if (getCurrentType() != null) {
			if (node.isInterface()) {
				List<Type> interfaces = node.getSuperInterfaceTypes();

				for (Type intType : interfaces) {
					AbstractList<ASTNode> features = new ArrayList<>();

					fireFoundRelationship(getCurrentType(),
							intType.resolveBinding(), GeneralizationPart.class,
							features);
				}
			} else {
				Type classType = node.getSuperclassType();

				if (classType != null) {
					AbstractList<ASTNode> features = new ArrayList<>();
					ITypeBinding binding = classType.resolveBinding();
					if (binding == null) return true;
					
					fireFoundRelationship(getCurrentType(), binding,
							GeneralizationPart.class, features);
				}
			}
		}
		return true;
	}

	/**
	 * @see edu.buffalo.cse.green.relationships.RelationshipRecognizer#getFlags()
	 */
	public RelationshipKind getFlags() {
		return RelationshipKind.Single;
	}
}