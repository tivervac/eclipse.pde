/*
 * Created on Oct 1, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.pde.internal.core.osgi.bundle;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.core.osgi.bundle.*;
import org.eclipse.pde.core.plugin.*;
import org.eclipse.pde.internal.core.AbstractModel;
import org.eclipse.pde.internal.core.plugin.*;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public abstract class BundlePluginModelBase extends AbstractModel
		implements
			IBundlePluginModelBase,
			IPluginModelFactory {
	private IBundleModel bundleModel;
	private ISharedExtensionsModel extensionsModel;
	private IBundlePluginBase bundlePluginBase;
	private IBuildModel buildModel;
	private BundleDescription fBundleDescription;
	private boolean enabled;
	public BundlePluginModelBase() {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.osgi.bundle.IBundlePluginModelBase#getBundleModel()
	 */
	public IBundleModel getBundleModel() {
		return bundleModel;
	}
	public IResource getUnderlyingResource() {
		return bundleModel.getUnderlyingResource();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.osgi.bundle.IBundlePluginModelBase#getExtensionsModel()
	 */
	public ISharedExtensionsModel getExtensionsModel() {
		return extensionsModel;
	}
	public void dispose() {
		if (bundleModel != null) {
			if (bundlePluginBase != null)
				bundleModel.removeModelChangedListener(bundlePluginBase);
			bundleModel.dispose();
			bundleModel = null;
		}
		if (extensionsModel != null) {
			extensionsModel.dispose();
			extensionsModel = null;
		}
		super.dispose();
	}
	public void save() {
		if (bundleModel != null && bundleModel instanceof IEditableModel) {
			IEditableModel emodel = (IEditableModel) bundleModel;
			if (emodel.isDirty())
				emodel.save();
		}
		if (extensionsModel != null
				&& extensionsModel instanceof IEditableModel) {
			IEditableModel emodel = (IEditableModel) extensionsModel;
			if (emodel.isDirty())
				emodel.save();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.osgi.bundle.IBundlePluginModelBase#setBundleModel(org.eclipse.pde.core.osgi.bundle.IBundleModel)
	 */
	public void setBundleModel(IBundleModel bundleModel) {
		if (this.bundleModel != null && bundlePluginBase != null) {
			this.bundleModel.removeModelChangedListener(bundlePluginBase);
		}
		this.bundleModel = bundleModel;
		if (bundlePluginBase != null)
			bundleModel.addModelChangedListener(bundlePluginBase);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.osgi.bundle.IBundlePluginModelBase#setExtensionsModel(org.eclipse.pde.core.plugin.IExtensionsModel)
	 */
	public void setExtensionsModel(ISharedExtensionsModel extensionsModel) {
		this.extensionsModel = extensionsModel;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#createPluginBase()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#getBuildModel()
	 */
	public IBuildModel getBuildModel() {
		return buildModel;
	}
	public void setBuildModel(IBuildModel buildModel) {
		this.buildModel = buildModel;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#getPluginBase()
	 */
	public IPluginBase getPluginBase() {
		return getPluginBase(true);
	}
	public IExtensions getExtensions() {
		return getPluginBase();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#getPluginBase(boolean)
	 */
	public IPluginBase getPluginBase(boolean createIfMissing) {
		if (bundlePluginBase == null && createIfMissing) {
			bundlePluginBase = (BundlePluginBase) createPluginBase();
			if (bundleModel != null)
				bundleModel.addModelChangedListener(bundlePluginBase);
			loaded = true;
		}
		return bundlePluginBase;
	}
	public IExtensions getExtensions(boolean createIfMissing) {
		return getPluginBase(createIfMissing);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#getPluginFactory()
	 */
	public IPluginModelFactory getPluginFactory() {
		return this;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.ISharedPluginModel#getFactory()
	 */
	public IExtensionsModelFactory getFactory() {
		if (extensionsModel != null)
			return extensionsModel.getFactory();
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.ISharedPluginModel#getInstallLocation()
	 */
	public String getInstallLocation() {
		if (bundleModel != null)
			return bundleModel.getInstallLocation();
		return null;
	}
	public URL getNLLookupLocation() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#getResourceString(java.lang.String)
	 */
	public String getResourceString(String key) {
		return key;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#isEditable()
	 */
	public boolean isEditable() {
		if (bundleModel != null && bundleModel.isEditable() == false)
			return false;
		if (extensionsModel != null && extensionsModel.isEditable() == false)
			return false;
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#isInSync()
	 */
	public boolean isInSync() {
		return ((bundleModel == null || bundleModel.isInSync()) && (extensionsModel == null || extensionsModel
				.isInSync()));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#isValid()
	 */
	public boolean isValid() {
		return ((bundleModel == null || bundleModel.isValid()) && (extensionsModel == null || extensionsModel
				.isValid()));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#load()
	 */
	public void load() throws CoreException {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#load(java.io.InputStream, boolean)
	 */
	public void load(InputStream source, boolean outOfSync)
			throws CoreException {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#reload(java.io.InputStream, boolean)
	 */
	public void reload(InputStream source, boolean outOfSync)
			throws CoreException {
	}
	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled
	 *            The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.core.AbstractModel#updateTimeStamp()
	 */
	protected void updateTimeStamp() {
	}
	public IPluginImport createImport() {
		PluginImport iimport = new PluginImport();
		iimport.setModel(this);
		iimport.setParent(getPluginBase());
		return iimport;
	}
	public IPluginLibrary createLibrary() {
		PluginLibrary library = new PluginLibrary();
		library.setModel(this);
		library.setParent(getPluginBase());
		return library;
	}
	public IPluginAttribute createAttribute(IPluginElement element) {
		if (extensionsModel != null)
			return extensionsModel.getFactory().createAttribute(element);
		return null;
	}
	public IPluginElement createElement(IPluginObject parent) {
		if (extensionsModel != null)
			return extensionsModel.getFactory().createElement(parent);
		return null;
	}
	public IPluginExtension createExtension() {
		if (extensionsModel != null)
			return extensionsModel.getFactory().createExtension();
		return null;
	}
	public IPluginExtensionPoint createExtensionPoint() {
		if (extensionsModel != null)
			return extensionsModel.getFactory().createExtensionPoint();
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IModel#isReconcilingModel()
	 */
	public boolean isReconcilingModel() {
		return false;
	}
	public boolean isBundleModel() {
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#getBundleDescription()
	 */
	public BundleDescription getBundleDescription() {
		return fBundleDescription;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.plugin.IPluginModelBase#setBundleDescription(org.eclipse.osgi.service.resolver.BundleDescription)
	 */
	public void setBundleDescription(BundleDescription description) {
		fBundleDescription = description;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IEditable#isDirty()
	 */
	public boolean isDirty() {
		if (bundleModel != null && (bundleModel instanceof IEditable)
				&& ((IEditable) bundleModel).isDirty())
			return true;
		if (extensionsModel != null && (extensionsModel instanceof IEditable)
				&& ((IEditable) extensionsModel).isDirty())
			return true;
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IEditable#save(java.io.PrintWriter)
	 */
	public void save(PrintWriter writer) {
		// Does nothing - individual models are saved instead
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.core.IEditable#setDirty(boolean)
	 */
	public void setDirty(boolean dirty) {
		//does nothing
	}
}