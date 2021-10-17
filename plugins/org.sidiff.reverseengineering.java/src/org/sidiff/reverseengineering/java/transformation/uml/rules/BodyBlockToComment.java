package org.sidiff.reverseengineering.java.transformation.uml.rules;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.sidiff.reverseengineering.java.transformation.uml.rulebase.JavaToUML;

public class BodyBlockToComment extends JavaToUML<Block, Element, Comment> {
	
	@Override
	public void apply(Block block) {
		if (!block.statements().isEmpty()) {
			String[] sortedWorts = rules.javaToUMLHelper.createBagOfWords(block.toString());
			
			if (sortedWorts.length > 0) {
				String operationBodyBOW = String.join(" ", sortedWorts);
				Comment umlComment = umlFactory.createComment();
				umlComment.setBody(operationBodyBOW);
				trafo.createModelElementFragment(block, umlComment);
			}
		}
	}

	@Override
	public void apply(Element element, Comment comment) {
		element.getOwnedComments().add(comment);
	}

	@Override
	public void link(Block block, Comment coment) throws ClassNotFoundException {
	}

}
