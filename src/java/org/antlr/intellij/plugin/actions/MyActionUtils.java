package org.antlr.intellij.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.intellij.plugin.profiler.ProfilerPanel;
import org.antlr.intellij.plugin.psi.LexerRuleRefNode;
import org.antlr.intellij.plugin.psi.LexerRuleSpecNode;
import org.antlr.intellij.plugin.psi.ParserRuleRefNode;
import org.antlr.intellij.plugin.psi.ParserRuleSpecNode;
import org.antlr.intellij.plugin.psi.RuleSpecNode;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.DecisionEventInfo;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.tool.Grammar;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyActionUtils {
	public static void selectedFileIsGrammar(AnActionEvent e) {
		VirtualFile vfile = getGrammarFileFromEvent(e);
		if ( vfile==null ) {
			e.getPresentation().setEnabled(false);
			return;
		}
		e.getPresentation().setEnabled(true); // enable action if we're looking at grammar file
		e.getPresentation().setVisible(true);
	}

	public static VirtualFile getGrammarFileFromEvent(AnActionEvent e) {
		VirtualFile[] files = LangDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		if ( files==null || files.length==0 ) return null;
		VirtualFile vfile = files[0];
		if ( vfile!=null && vfile.getName().endsWith(".g4") ) {
			return vfile;
		}
		return null;
	}

	public static int getMouseOffset(MouseEvent mouseEvent, Editor editor) {
		Point point=new Point(mouseEvent.getPoint());
		LogicalPosition pos=editor.xyToLogicalPosition(point);
		return editor.logicalPositionToOffset(pos);
	}

	public static int getMouseOffset(Editor editor) {
		Point mousePosition = editor.getContentComponent().getMousePosition();
		LogicalPosition pos=editor.xyToLogicalPosition(mousePosition);
		int offset = editor.logicalPositionToOffset(pos);
		return offset;
	}

	@NotNull
	public static List<RangeHighlighter> getRangeHighlightersAtOffset(Editor editor, int offset) {
		MarkupModel markupModel = editor.getMarkupModel();
		// collect all highlighters and combine to make a single tool tip
		List<RangeHighlighter> highlightersAtOffset = new ArrayList<RangeHighlighter>();
		for (RangeHighlighter r : markupModel.getAllHighlighters()) {
			int a = r.getStartOffset();
			int b = r.getEndOffset();
//			System.out.printf("#%d: %d..%d %s\n", i, a, b, r.toString());
			if (offset >= a && offset < b) { // cursor is over some kind of highlighting
				highlightersAtOffset.add(r);
			}
		}
		return highlightersAtOffset;
	}

	public static DecisionEventInfo getHighlighterWithDecisionEventType(List<RangeHighlighter> highlighters, Class decisionEventType) {
		for (RangeHighlighter r : highlighters) {
			DecisionEventInfo eventInfo = r.getUserData(ProfilerPanel.DECISION_EVENT_INFO_KEY);
			if (eventInfo != null) {
				if (eventInfo.getClass() == decisionEventType) {
					return eventInfo;
				}
			}
		}
		return null;
	}

	public static ParserRuleRefNode getParserRuleSurroundingRef(AnActionEvent e) {
		RuleSpecNode ruleSpecNode = getRuleSurroundingRef(e, ParserRuleSpecNode.class);
		if ( ruleSpecNode==null ) return null;
		// find the name of rule under ParserRuleSpecNode
		return PsiTreeUtil.findChildOfType(ruleSpecNode, ParserRuleRefNode.class);
	}

	public static LexerRuleRefNode getLexerRuleSurroundingRef(AnActionEvent e) {
		RuleSpecNode ruleSpecNode = getRuleSurroundingRef(e, LexerRuleSpecNode.class);
		if ( ruleSpecNode==null ) return null;
		// find the name of rule under ParserRuleSpecNode
		return PsiTreeUtil.findChildOfType(ruleSpecNode, LexerRuleRefNode.class);
	}

	public static RuleSpecNode getRuleSurroundingRef(AnActionEvent e,
	                                                 final Class<? extends RuleSpecNode> ruleSpecNodeClass)
	{
		PsiElement selectedPsiNode = getSelectedPsiElement(e);
		System.out.println("selectedPsiNode: "+selectedPsiNode);

		if ( selectedPsiNode==null ) { // didn't select a node in parse tree
			return null;
		}

		// find root of rule def
		if ( selectedPsiNode.getClass()!=ruleSpecNodeClass ) {
			selectedPsiNode = PsiTreeUtil.findFirstParent(selectedPsiNode, new Condition<PsiElement>() {
				@Override
				public boolean value(PsiElement psiElement) {
					return psiElement.getClass()==ruleSpecNodeClass;
				}
			});
			if ( selectedPsiNode==null ) { // not in rule I guess.
				return null;
			}
			// found rule
		}
		return (RuleSpecNode)selectedPsiNode;
	}

	public static PsiElement getSelectedPsiElement(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		if ( editor==null ) { // not in editor
			PsiElement selectedNavElement = e.getData(LangDataKeys.PSI_ELEMENT);
			// in nav bar?
			if ( selectedNavElement==null || !(selectedNavElement instanceof ParserRuleRefNode) ) {
				return null;
			}
			return selectedNavElement;
		}

		// in editor
		PsiFile file = e.getData(LangDataKeys.PSI_FILE);
		if ( file==null ) {
			return null;
		}

		//		System.out.println("caret offset = "+editor.getCaretModel().getOffset());
		PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
		//		System.out.println("sel el: "+selectedPsiRuleNode);
		return el;
	}

	public static List<TerminalNode> getAllRuleRefNodes(Parser parser, ParseTree tree, String ruleName) {
		List<TerminalNode> nodes = new ArrayList<TerminalNode>();
		Collection<ParseTree> ruleRefs;
		if ( Grammar.isTokenName(ruleName) ) {
			ruleRefs = XPath.findAll(tree, "//lexerRuleBlock//TOKEN_REF", parser);
		}
		else {
			ruleRefs = XPath.findAll(tree, "//ruleBlock//RULE_REF", parser);
		}
		for (ParseTree node : ruleRefs) {
			TerminalNode terminal = (TerminalNode)node;
			Token rrefToken = terminal.getSymbol();
			String r = rrefToken.getText();
			if ( r.equals(ruleName) ) {
				nodes.add(terminal);
			}
		}
		if ( nodes.size()==0 ) return null;
		return nodes;
	}
}
