/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.editor.targetdefinition;

import org.eclipse.pde.internal.ui.PDEUIMessages;

import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Second page in the target definition editor.  Allows for editing of the included bundles in
 * the target
 * @see TargetEditor
 * @see ContentSection
 */
public class ContentPage extends FormPage {

	public static final String PAGE_ID = "content"; //$NON-NLS-1$

	public ContentPage(TargetEditor editor) {
		super(editor, PAGE_ID, PDEUIMessages.ContentPage_0); 
	}

	/**
	 * @return The target model backing this editor
	 */
	public ITargetDefinition getTarget() {
		return ((TargetEditor) getEditor()).getTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText(PDEUIMessages.ContentPage_1); 
		PDEPlugin.getDefault().getLabelProvider().connect(this);
		form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_TARGET_DEFINITION));
		toolkit.decorateFormHeading(form.getForm());
		fillBody(managedForm, toolkit);
		// TODO Finish help
		((TargetEditor) getEditor()).contributeToToolbar(managedForm.getForm(), ""); //$NON-NLS-1$
		((TargetEditor) getEditor()).addForm(managedForm);
		form.updateToolBar();
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(), IHelpContextIds.TARGET_OVERVIEW_PAGE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	public void dispose() {
		PDEPlugin.getDefault().getLabelProvider().disconnect(this);
		super.dispose();
	}

	private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
		Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 1));
		managedForm.addPart(new ContentSection(this, body));
	}

	// TODO Hook up help toolbar action
//	protected String getHelpResource() {
//		return "/org.eclipse.pde.doc.user/guide/tools/editors/target_definition_editor/overview.htm"; //$NON-NLS-1$
//	}

}
