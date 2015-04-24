/** Metadata Tree Browser Aspect, display the overview listing of sets wich an expandable list **/
jQuery(document).ready(function() {

   // Add toggle controls to all elements which have children.
   jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list li").each(function() {
      if (jQuery(this).next().has('ul').length > 0) {
         jQuery(this).prepend("<a class=\"toggle\" href=\"javascript: void(0);\">[+]</a> ");
      }
   });
   
   // Toggle the state of a control, showing the children and switching the display character used.
   jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list .toggle").click(function() {
      if (jQuery(this).text().indexOf('[-]') > -1) {
         // Collapse Children
         jQuery(this).text('[+]');
         
         // Variable speed based upon how many children.
         count = jQuery(this).parent().next().has('li ul li').length;
         jQuery(this).parent().next().has('li').slideUp(count * 200 + 200);
         

      } else {
         // Expand Children
         jQuery(this).text('[-]');
         
         count = jQuery(this).parent().next().has('li ul li').length;
         jQuery(this).parent().next().has('li').slideDown(count * 200 + 200);
      }
   });
   
   // preset all toggles to be closed. This will only show the first order list.
   jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list a.toggle").each(function () {
      jQuery(this).parent().next().has('li').hide();     
   });
   
   // If there is only one top level element, auto expand it.
   if (jQuery("ul#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list.root-list").children().length <= 2) {
      jQuery("ul#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list.root-list").children().last().show();
      jQuery("ul#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list.root-list a.toggle").first().text("[-]");
   }
   
   // Add the expand all button
   jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_div_metadata-tree-browser-overview").prepend("<a class=\"expand-all ds-button-field btn btn-default\" href=\"javascript: void(0);\">Expand All</a>");
   jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_div_metadata-tree-browser-overview .expand-all").click(function() {
      if (jQuery(this).text().indexOf('Expand') > -1) { 
         // Expand all Children
         jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list a.toggle").each(function () {
            count = jQuery(this).parent().next().has('li ul li').length;
            jQuery(this).parent().next().has('li').slideDown(count * 200 + 200);
            jQuery(this).text('[-]');
         });
         jQuery(this).text('Collapse All');
         
      } else {
         // Collapse all Children
         jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_list_overview-list a.toggle").each(function () {
            count = jQuery(this).parent().next().has('li ul li').length;
            jQuery(this).parent().next().has('li').slideUp(count * 200 + 200);
            jQuery(this).text('[+]');
         });
         jQuery(this).text('Expand All');
      }
      
   });
   
});