/*
 * BuildToolsPackagePanel.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */


package org.rstudio.studio.client.projects.ui.prefs.buildtools;

import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.core.client.widget.ThemedButton;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.common.HelpLink;
import org.rstudio.studio.client.common.PackagesHelpLink;
import org.rstudio.studio.client.projects.model.RProjectBuildOptions;
import org.rstudio.studio.client.projects.model.RProjectConfig;
import org.rstudio.studio.client.projects.model.RProjectOptions;
import org.rstudio.studio.client.projects.ui.prefs.ProjectPreferencesDialogResources;
import org.rstudio.studio.client.workbench.WorkbenchContext;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;


public class BuildToolsPackagePanel extends BuildToolsPanel
{
   public BuildToolsPackagePanel()
   {
      RStudioGinjector.INSTANCE.injectMembers(this);
      
      ProjectPreferencesDialogResources RES =
                              ProjectPreferencesDialogResources.INSTANCE;
      
      pathSelector_ = new DirectorySelector("Package directory:");
      pathSelector_.getElement().getStyle().setMarginBottom(10, Unit.PX);
      add(pathSelector_); 
      pathSelector_.addValueChangeHandler(new ValueChangeHandler<String>() {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            if (pathSelector_.getText().equals(
                           workbenchContext_.getActiveProjectDir().getPath())) 
            {
               pathSelector_.setText("");
            }
         }
         
      });
      
      roxygenizePanel_ = new VerticalPanel();
      roxygenizePanel_.addStyleName(RES.styles().buildToolsRoxygenize());
      HorizontalPanel rocletPanel = new HorizontalPanel();
      chkUseRoxygen_ = checkBox("Generate documentation with Roxygen");
      rocletPanel.add(chkUseRoxygen_);
      btnConfigureRoxygen_ = new ThemedButton("Configure...");
      btnConfigureRoxygen_.addClickHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event)
         {
            new BuildToolsRoxygenOptionsDialog(
               roxygenOptions_,
               new OperationWithInput<BuildToolsRoxygenOptions>() {

                  @Override
                  public void execute(BuildToolsRoxygenOptions input)
                  {
                     roxygenOptions_ = input;
                     chkUseRoxygen_.setValue(input.getRocletRd() || 
                                             input.getRocletCollate() || 
                                             input.getRocletNamespace());
                     
                  }
                  
               }).showModal();
            
         } 
      });
      rocletPanel.add(btnConfigureRoxygen_);
      roxygenizePanel_.add(rocletPanel);
      add(roxygenizePanel_);
      
      
      add(installAdditionalArguments_ = new AdditionalArguments(
        new SafeHtmlBuilder().appendHtmlConstant(
          "Build and Reload &mdash; R CMD INSTALL additional options:").toSafeHtml()));
     
      add(checkAdditionalArguments_ = new AdditionalArguments(
            new SafeHtmlBuilder().appendHtmlConstant(
                "Check Package &mdash; R CMD check additional options:").toSafeHtml()));
      
      add(buildAdditionalArguments_ = new AdditionalArguments(
        new SafeHtmlBuilder().appendHtmlConstant(
          "Build Source Package &mdash; R CMD build additional options:").toSafeHtml()));
           
      add(buildBinaryAdditionalArguments_ = new AdditionalArguments(
        new SafeHtmlBuilder().appendHtmlConstant(
          "Build Binary Package &mdash; R CMD INSTALL additional options:").toSafeHtml()));
      
      
      HelpLink packagesHelpLink = new PackagesHelpLink();
      packagesHelpLink.getElement().getStyle().setMarginTop(7, Unit.PX);
      add(packagesHelpLink);
     }
   
   @Inject
   public void initialize(WorkbenchContext workbenchContext)
   {
      workbenchContext_ = workbenchContext;
   }
   
   @Override
   protected void provideDefaults()
   {
      installAdditionalArguments_.setText("--no-multiarch --with-keep.source");
   }

   @Override
   void load(RProjectOptions options)
   {
      RProjectConfig config = options.getConfig();
      pathSelector_.setText(config.getPackagePath());
      installAdditionalArguments_.setText(config.getPackageInstallArgs());
      buildAdditionalArguments_.setText(config.getPackageBuildArgs());
      buildBinaryAdditionalArguments_.setText(config.getPackageBuildBinaryArgs());
      checkAdditionalArguments_.setText(config.getPackageCheckArgs());
   
      roxygenOptions_ = new BuildToolsRoxygenOptions(
            config.getPackageRoxygenzieRd(),
            config.getPackageRoxygenizeCollate(),
            config.getPackageRoxygenizeNamespace(),
            options.getBuildOptions().getAutoRogyginizeOptions());
       
      boolean showRoxygenize = config.hasPackageRoxygenize() ||
                               options.getBuildContext().isRoxygen2Installed();
      roxygenizePanel_.setVisible(showRoxygenize);
      chkUseRoxygen_.setValue(config.hasPackageRoxygenize());
      chkUseRoxygen_.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            if (event.getValue())
            {
               if (!roxygenOptions_.hasActiveRoclet())
                  roxygenOptions_.setRocletRd(true);
               btnConfigureRoxygen_.click();
            }
            else
            {
               roxygenOptions_.clearRoclets();
            }
         }
      });
   }

   @Override
   void save(RProjectOptions options)
   {
      RProjectConfig config = options.getConfig();
      config.setPackagePath(pathSelector_.getText());
      config.setPackageInstallArgs(installAdditionalArguments_.getText());
      config.setPackageBuildArgs(buildAdditionalArguments_.getText());
      config.setPackageBuildBinaryArgs(buildBinaryAdditionalArguments_.getText());
      config.setPackageCheckArgs(checkAdditionalArguments_.getText());
      config.setPackageRoxygenize(roxygenOptions_.getRocletRd(),
                                  roxygenOptions_.getRocletCollate(),
                                  roxygenOptions_.getRocletNamespace());
      RProjectBuildOptions buildOptions = options.getBuildOptions();
      buildOptions.setAutoRoxyginizeOptions(
                                       roxygenOptions_.getAutoRoxygenize());
   }

   private PathSelector pathSelector_;
   
   private AdditionalArguments installAdditionalArguments_;
   private AdditionalArguments buildAdditionalArguments_;
   private AdditionalArguments buildBinaryAdditionalArguments_;
   private AdditionalArguments checkAdditionalArguments_;
     
   private BuildToolsRoxygenOptions roxygenOptions_;
   
   private VerticalPanel roxygenizePanel_;
   private CheckBox chkUseRoxygen_;
   private ThemedButton btnConfigureRoxygen_;
   
   private WorkbenchContext workbenchContext_;
}
