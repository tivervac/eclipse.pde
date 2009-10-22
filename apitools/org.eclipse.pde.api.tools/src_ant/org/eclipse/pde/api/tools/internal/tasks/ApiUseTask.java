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

package org.eclipse.pde.api.tools.internal.tasks;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiBaseline;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiElement;
import org.eclipse.pde.api.tools.internal.provisional.search.ApiSearchEngine;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchReporter;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchRequestor;
import org.eclipse.pde.api.tools.internal.search.ApiDescriptionModifier;
import org.eclipse.pde.api.tools.internal.search.SkippedComponent;
import org.eclipse.pde.api.tools.internal.search.UseSearchRequestor;
import org.eclipse.pde.api.tools.internal.search.XmlSearchReporter;
import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * Ant task for performing the API use analysis of a given Eclipse SDK
 * 
 * @since 1.0.1
 */
public final class ApiUseTask extends CommonUtilsTask {
	
	/**
	 * If api references should be considered in the search
	 */
	private boolean considerapi = false;
	/**
	 * If internal references should be considered in the search
	 */
	private boolean considerinternal = false;
	/**
	 * Set of project names that were not searched
	 */
	private TreeSet notsearched = null;
	/**
	 * The regex pattern to use to compose the scope
	 */
	private String scopepattern = null;
	/**
	 * The regex pattern to use to compose the reference set of component ids
	 */
	private String referencepattern = null;
	
	/**
	 * handle to the baseline install dir to delete after the scan completes
	 */
	private File baselinedir = null;
	
	/**
	 * Package name patterns (regular expressions) to consider as API or <code>null</code> if none.
	 */
	private String[] apiPatterns = null;
	
	/**
	 * Package name patterns (regular expressions) to consider as internal or <code>null</code> if none.
	 */
	private String[] internalPatterns = null;
	
	/**
	 * Archive name patterns to not scan during analysis.
	 * Formulation:
	 * <pre>
	 * <bundle name>:<path to jar>
	 * </pre>
	 */
	private String[] archivePatterns = null;
	
	/**
	 * Set the location of the current product you want to search.
	 * 
	 * <p>It can be a .zip, .jar, .tgz, .tar.gz file, or a directory that corresponds to 
	 * the Eclipse installation folder. This is the directory is which you can find the 
	 * Eclipse executable.
	 * </p>
	 *
	 * @param location the given location for the baseline to analyze
	 */
	public void setLocation(String location) {
		this.currentBaselineLocation = location;
	}

	/**
	 * Set the regular expression pattern used to build the scope of elements to search for 
	 * references from in the product location.
	 * 
	 * <p>
	 * The pattern must be a well-formatted regular expression as
	 * defined here: http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html 
	 * </p>
	 * @param scopepattern
	 */
	public void setScopePattern(String scopepattern) {
		this.scopepattern = scopepattern;
	}
	
	/**
	 * Set the regular expression pattern used to build the scope of elements to search for 
	 * references to in the product location.
	 * 
	 * <p>
	 * The pattern must be a well-formatted regular expression as
	 * defined here: http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html 
	 * </p>
	 * @param referencepattern
	 */
	public void setReferencePattern(String referencepattern) {
		this.referencepattern = referencepattern;
	}
	
	/**
	 * Set the output location where the reports will be generated.
	 * 
	 * <p>Once the task is completed, reports are available in this directory using a structure
	 * similar to the filter root. A sub-folder is created for each component that has problems
	 * to be reported. Each sub-folder contains a file called "report.xml". </p>
	 * 
	 * <p>A special folder called "allNonApiBundles" is also created in this folder that contains a xml file called
	 * "report.xml". This file lists all the bundles that are not using the api tooling nature.</p>
	 * 
	 * @param baselineLocation the given location for the reference baseline to analyze
	 */
	public void setReport(String reportlocation) {
		this.reportLocation = reportlocation;
	}
	
	/**
	 * Set the debug value.
	 * <p>The possible values are: <code>true</code>, <code>false</code></p>
	 * <p>Default is <code>false</code>.</p>
	 *
	 * @param debugValue the given debug value
	 */
	public void setDebug(String debugValue) {
		this.debug = Boolean.toString(true).equals(debugValue); 
	}
	
	/**
	 * Sets if references to API types should be considered in the search.
	 * <p>The possible values are: <code>true</code>, <code>false</code></p>
	 * <p>Default is <code>false</code>.</p>
	 * 
	 * @param considerapi the given value
	 */
	public void setConsiderAPI(String considerapi) {
		this.considerapi = Boolean.toString(true).equals(considerapi);
	}
	
	/**
	 * Sets any package name patterns to consider as API packages.
	 * 
	 * @param patterns comma separated list of regular expressions or <code>null</code>
	 */
	public void setApiPatterns(String patterns) {
		apiPatterns = parsePatterns(patterns);
	}
	
	/**
	 * Sets if references to internal types should be considered in the search.
	 * <p>The possible values are: <code>true</code>, <code>false</code></p>
	 * <p>Default is <code>false</code>.</p>
	 * 
	 * @param considerapi the given value
	 */
	public void setConsiderInternal(String considerinternal) {
		this.considerinternal = Boolean.toString(true).equals(considerinternal);
	}
	
	/**
	 * Sets any package name patterns to consider as internal packages.
	 * 
	 * @param patterns comma separated list of regular expressions or <code>null</code>
	 */
	public void setInternalPatterns(String patterns) {
		internalPatterns = parsePatterns(patterns);	
	}
	
	/**
	 * Sets any archive name patterns to not scan during the analysis.
	 * 
	 * @param patterns
	 */
	public void setArchivePatterns(String patterns) {
		archivePatterns = parsePatterns(patterns);
	}
	
	/**
	 * Parses and returns patterns as an array of Strings or <code>null</code> if none.
	 * 
	 * @param patterns comma separated list or <code>null</code>
	 * @return individual patterns or <code>null</code>
	 */
	private String[] parsePatterns(String patterns) {
		if (patterns == null || patterns.trim().length() == 0) {
			return null;
		}
		String[] strings = patterns.split(","); //$NON-NLS-1$
		List list = new ArrayList();
		for (int i = 0; i < strings.length; i++) {
			String pattern = strings[i].trim();
			if (pattern.length() > 0) {
				list.add(pattern);
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	/**
	 * @see org.eclipse.pde.api.tools.internal.tasks.UseTask#assertParameters()
	 */
	protected void assertParameters() throws BuildException {
		if (this.reportLocation == null) {
			StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			writer.println(NLS.bind(
					Messages.ApiUseTask_missing_report_location, 
					new String[] {this.reportLocation}));
			writer.flush();
			writer.close();
			throw new BuildException(String.valueOf(out.getBuffer()));
		}
		if (this.currentBaselineLocation == null) {
			StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			writer.println(NLS.bind(
					Messages.ApiUseTask_missing_baseline_argument, 
					new String[] {this.currentBaselineLocation}));
			writer.flush();
			writer.close();
			throw new BuildException(String.valueOf(out.getBuffer()));
		}
		//stop if we don't want to see anything
		if(!considerapi && !considerinternal) {
			throw new BuildException(Messages.UseTask_no_scan_both_types_not_searched_for);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		assertParameters();
		writeDebugHeader();
		cleanReportLocation();
		
		IApiBaseline baseline = getBaseline(CURRENT_BASELINE_NAME, this.currentBaselineLocation);
		IApiSearchReporter reporter = new XmlSearchReporter(this.reportLocation, this.debug);
		try {
			Set ids = new HashSet();
			TreeSet scope = new TreeSet(Util.componentsorter);
			getContext(baseline, ids, scope);
			ApiSearchEngine engine = new ApiSearchEngine();
			UseSearchRequestor requestor = new UseSearchRequestor(
					ids,
					(IApiElement[]) scope.toArray(new IApiElement[scope.size()]), 
					getSearchFlags());
			requestor.setJarPatterns(archivePatterns);
			// override API descriptions as required
			if (apiPatterns != null || internalPatterns != null) {
				// modify API descriptions
				ApiDescriptionModifier visitor = new ApiDescriptionModifier(internalPatterns, apiPatterns);
				IApiComponent[] components = baseline.getApiComponents();
				for (int i = 0; i < components.length; i++) {
					IApiComponent component = components[i];
					if (!component.isSystemComponent() && !component.isSourceComponent()) {
						visitor.setApiDescription(component.getApiDescription());
						component.getApiDescription().accept(visitor, null);
					}
				}
			}
			
			ApiSearchEngine.setDebug(this.debug);
			engine.search(baseline, requestor, reporter, null);
		}
		catch(CoreException ce) {
			throw new BuildException(Messages.ApiUseTask_search_engine_problem, ce);
		}
		finally {
			if(baseline != null) {
				baseline.dispose();
				deleteBaseline(this.currentBaselineLocation, this.baselinedir);
			}
			reporter.reportNotSearched((IApiElement[]) this.notsearched.toArray(new IApiElement[this.notsearched.size()]));
		}
	}
	
	/**
	 * Returns if we should add the given component to our search scope
	 * @param component
	 * @param pattern
	 * @param allowresolve
	 * @return true if the given component should be considered, false otherwise
	 * @throws CoreException
	 */
	boolean acceptComponent(IApiComponent component, Pattern pattern, boolean allowresolve) throws CoreException {
		if(!allowresolve) {
			ResolverError[] errors = component.getErrors();
			if(errors != null) {
				this.notsearched.add(new SkippedComponent(component.getId(), component.getVersion(), errors)); 
				return false;
			}
		}
		if(component.isSystemComponent()) {
			return false;
		}
		if(pattern != null) {
			return pattern.matcher(component.getId()).matches();
		}
		return true;
	}
	
	/**
	 * Collects the scope elements and reference ids in one pass
	 * @param baseline the baseline to check the components for
	 * @param ids the live set of reference ids
	 * @param scope the live set of elements for the scope
	 * @throws CoreException
	 */
	private void getContext(IApiBaseline baseline, Set ids, Set scope) throws CoreException {
		IApiComponent[] components = baseline.getApiComponents();
		this.notsearched = new TreeSet(Util.componentsorter);
		Pattern pattern = null, pattern2 = null;
		if(this.referencepattern != null) {
			pattern = Pattern.compile(this.referencepattern);
		}
		if(this.scopepattern != null) {
			pattern2 = Pattern.compile(this.scopepattern);
		}
		for (int i = 0; i < components.length; i++) {
			if(acceptComponent(components[i], pattern, true)) {
				ids.add(components[i].getId());
			}
			if(acceptComponent(components[i], pattern2, false)) {
				scope.add(components[i]);
			}
			else {
				this.notsearched.add(new SkippedComponent(components[i].getId(), components[i].getVersion(), components[i].getErrors()));
			}
		}
	}
	
	/**
	 * Returns the set of search flags to use for the {@link IApiSearchRequestor}
	 * 
	 * @return the set of flags to use
	 */
	protected int getSearchFlags() {
		int flags = (this.considerapi ? IApiSearchRequestor.INCLUDE_API : 0);
		flags |= (this.considerinternal ? IApiSearchRequestor.INCLUDE_INTERNAL : 0);
		return flags;
	}
	
	/**
	 * Prepares and creates and new baseline with the given name from the given location. The
	 * returned {@link IApiBaseline} is not checked for resolution errors or consistency. If <code>null</code>
	 * is passed in as a location <code>null</code> is returned.
	 * 
	 * @param name the name to give to the baseline
	 * @param location the location the baseline should be prepared from. If <code>null</code> is passed in, <code>null</code>
	 * is returned
	 * @return a new {@link IApiBaseline} with the given name from the given location or <code>null</code> if the given location
	 * is <code>null</code>
	 */
	protected IApiBaseline getBaseline(String name, String location) {
		if(location == null) {
			return null;
		}
		//extract the baseline to examine
		long time = 0;
		if (this.debug) {
			time = System.currentTimeMillis();
			System.out.println("Preparing '"+name+"' baseline installation..."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File installdir = extractSDK(name, location);
		if (this.debug) {
			System.out.println("done in: " + (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
			time = System.currentTimeMillis();
		}
		//create the baseline to examine
		if(this.debug) {
			time = System.currentTimeMillis();
			System.out.println("Creating '"+name+"' baseline..."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		IApiBaseline baseline = createBaseline(name, getInstallDir(installdir), this.eeFileLocation);
		if (this.debug) {
			System.out.println("done in: " + (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.baselinedir = installdir;
		return baseline;
	}
	
	/**
	 * Cleans the report location specified by the parameter {@link CommonUtilsTask#reportLocation}
	 */
	protected void cleanReportLocation() {
		if(this.reportLocation == null) {
			return;
		}
		long time = 0;
		if(this.debug) {
			time = System.currentTimeMillis();
			System.out.println("Cleaning report location..."); //$NON-NLS-1$
		}
		File file = new File(this.reportLocation);
		if(file.exists()) {
			Util.delete(file);
		}
		if(this.debug) {
			System.out.println("done in: "+ (System.currentTimeMillis() - time) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Writes a general header of debug information iff the debug flag is set to true
	 */
	protected void writeDebugHeader() {
		if (this.debug) {
			System.out.println("Product location to search : " + this.currentBaselineLocation); //$NON-NLS-1$
			System.out.println("Report location : " + this.reportLocation); //$NON-NLS-1$
			System.out.println("Searching for API references : " + this.considerapi); //$NON-NLS-1$
			System.out.println("Searching for internal references : " + this.considerinternal); //$NON-NLS-1$
			if(this.scopepattern == null) {
				System.out.println("No scope pattern defined - searching all bundles"); //$NON-NLS-1$
			}
			else {
				System.out.println("Scope pattern : " + this.scopepattern); //$NON-NLS-1$
			}
			if(this.referencepattern == null) {
				System.out.println("No baseline pattern defined - reporting references to all bundles"); //$NON-NLS-1$
			}
			else {
				System.out.println("Baseline pattern : " + this.referencepattern); //$NON-NLS-1$
			}
			System.out.println("-----------------------------------------------------------------------------------------------------"); //$NON-NLS-1$
		}
	}
}
