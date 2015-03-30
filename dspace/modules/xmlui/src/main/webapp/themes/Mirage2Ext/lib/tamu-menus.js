jQuery(document).ready(function(){
	//create a cookie with name: @name and value: @value, to expire in @years years
	function setCookie(name,value,years) {
		exp = new Date();
		exp.setTime(exp.getTime()+(years*365*24*60*60*1000));
		document.cookie = name+"="+value+";path="+window.location.pathname+";expires="+exp.toGMTString();
	}

	//get cookie by name: @name
	function getCookie(name) {
		var cookieCutter = name+"=";
		var temp = document.cookie.split(";");
		for (var i=0;i<temp.length;i++) {
			var cookie = temp[i].trim();
			if (cookie.indexOf(cookieCutter)==0) {
				return cookie.substring(cookieCutter.length,cookie.length);
			}
		}
		return null;
	}

	//alert("Render mode: "+ document.compatMode + "\nUser Agent: " + navigator.userAgent);
	
	
	//First, some css that couldn't be achieved with css selectors
	jQuery("table:not(.ds-includeSet-metadata-table) tr td:has(span[class=bold])").css({ textAlign:"right", verticalAlign:"top" });
	jQuery("table.ds-includeSet-metadata-table tr td:has(span[class=bold])").css({ textAlign:"left", verticalAlign:"top" });
	jQuery("td.ds-table-cell:has(strong)").css({ backgroundColor:"#DDDDDD", padding:"3px" });
	jQuery("form#aspect_submission_Submissions_div_submissions div#aspect_submission_Submissions_div_start-submision p.ds-paragraph a").css({fontWeight:"bold", color:"#374F5D"});
	jQuery("div#aspect_artifactbrowser_CollectionViewer_div_collection-view div.detail-view + p.ds-paragraph a").css({backgroundColor:"#2E6DA2", color:"white", border:"3px outset white", padding:"3px"});
	jQuery("div#aspect_artifactbrowser_CollectionViewer_div_collection-view div.detail-view + p.ds-paragraph").css({marginTop:"15px"});
	
	/* added in order to fix disappearing text in IE8 compatibility-mode */
    jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view li.ds-artifact-item").css("position", "relative");

	
	//eliminate empty menus; div.menuslider div.ds-option-set whose ul contains no li - was used for the unpopular hover-accordion menu sliders
	/*
	jQuery("div.menuslider:has(div.ds-option-set:has(ul.ds-option-list:not(:has(li))))").remove();
	jQuery("div.menuslider:has(div:has(ul:not(:has(li))))").css({ border:"5px solid red"});
	*/
	
	//eliminate ListPlus and ListMinus for the community/collection hierarchy when there is no ul contained in the li.
	//replace them with spacers to make the hierarchy look flush
	//div#aspect_artifactbrowser_CommunityViewer_div_community-view
	//div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser	
	jQuery("div.ds-static-div li:not(:has(ul))").children("p.ListPlus").after("<p class=\"ListEmpty\"></p>");
	jQuery("div.ds-static-div li:not(:has(ul))").children("p.ListMinus, p.ListPlus").remove();
	
	//close the community/collection hierarchy by default
	jQuery(document).hideAllCommColl();
	//close to second level the community/collection hierarchy on community view page.
	jQuery(document).hideCommCollInCommViewTo2ndLevel();
	
	//show the Expand All link
	jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p#expand_all_clicker").show();
	jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p#expand_all_clicker").show();
	
	//eliminate the expansion clickers when they appear in the context of the front page
	jQuery("div#right_content_column div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p#expand_all_clicker, div#right_content_column div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p#collapse_all_clicker").remove();
	
	//open "Browse" menu option by default
	/*
	jQuery("h3#menu_heading_0").removeClass("closed");
	jQuery("h3#menu_heading_0").addClass("open");
	jQuery("h3#menu_heading_0").next().show()
	jQuery("h3#menu_heading_0").find("img.menu_nav_icon_open").show();
	jQuery("h3#menu_heading_0").find("img.menu_nav_icon_closed").hide();*/
	//first, close everything.
	jQuery("h3.ds-option-set-head-clickable").siblings("div.ds-option-set").hide();
	jQuery("h3.ds-option-set-head-clickable").addClass("closed");
	jQuery("h3.ds-option-set-head-clickable").removeClass("open");
	jQuery("h3.ds-option-set-head-clickable").find("img.menu_nav_icon_open").hide();
	jQuery("h3.ds-option-set-head-clickable").find("img.menu_nav_icon_closed").show();
	//then, open just the "Browse" option.	
	jQuery("h3#menu_heading_0").siblings("div.ds-option-set").show();
	jQuery("h3#menu_heading_0").addClass("open");
	jQuery("h3#menu_heading_0").removeClass("closed");
	jQuery("h3#menu_heading_0").find("img.menu_nav_icon_open").show();
	jQuery("h3#menu_heading_0").find("img.menu_nav_icon_closed").hide();
	
	//The unpopular hover-sliding collapsable menus on left navigation bar
	/*
	jQuery("div.menuslider h3.closed").bind("mouseover",function(){
	    //hide any open sub-menu
	    jQuery("h3.open").next().slideUp("slow");
		jQuery("h3.open").find("img.menu_nav_icon_open").hide();
		jQuery("h3.open").find("img.menu_nav_icon_closed").show();
	    jQuery("h3.open").addClass("closed");
	    jQuery("h3.open").removeClass("open");
	    
	    //show this sub-menu
	    jQuery(this).next("div.ds-option-set").slideDown("slow");
	    jQuery(this).addClass("open");
	    jQuery(this).removeClass("closed");
		jQuery(this).find("img.menu_nav_icon_open").show();
		jQuery(this).find("img.menu_nav_icon_closed").hide();
		
		jQuery(document).myReBind();
	});*/
		
	
		

	//Click to open sliders on the left menu
	jQuery(".ds-option-set-head-clickable").on("click", function() {
		jQuery(this).toggleClass("open").toggleClass("closed")
		jQuery(this).find("img.menu_nav_icon_open").toggle();
		jQuery(this).find("img.menu_nav_icon_closed").toggle();
		jQuery(this).next(".ds-option-set").slideToggle(300);
	});	
	
	//The navigation hover text contextual feedback at the top of the page	- these got removed from the design for aesthetic reasons.
	/*
	jQuery("a.top-icon").hover(function(){
		jQuery(this).find("span").each(function() {
			jQuery(this).addClass("top-icon-text-shown");
		});
	},function(){
		jQuery(this).find("span").each(function() {
			jQuery(this).removeClass("top-icon-text-shown");
		});
	});
	*/
	
	
	
	//community/collection hierarchy
	//expansion with the plus sign (or horizontal arrow)
	jQuery("p.ListPlus").click(function(){
		jQuery(this).hide();
		jQuery(this).next("p.ListMinus").show();
		if(navigator.userAgent.match("MSIE 6")) //slideDown animation doesn't work in IE6.
		{
		    jQuery(this).parent().find("p.ListPlus").hide();
		    jQuery(this).parent().find("p.ListMinus").show();
		    jQuery(this).parent().find("p.ListMinus + span.bold ~ ul").show();
		}
		else
		{
		    jQuery(this).parent().children("ul").slideDown("fast");
		}
		//jQuery(this).parent().find("ul").css("min-height", "2 !important"); //Trying to get things to appear in IE6,7,8!
		//jQuery(this).parent().find("li").css("min-height", "2 !important"); //Trying to get things to appear in IE6,7,8!
		
		// We were unable to get the sub-elements to appear correctly in IE8 compatibility mode.
		//jQuery(this).parent().find("ul").css("display", "block"); //might be necessary for IE8 in compatibility mode which imperfectly pretends to be IE7
		//jQuery(this).parent().find("ul").css("position", "relative");
		//jQuery(this).parent().find("li").css("display", "block"); //might be necessary for IE8 in compatibility mode which imperfectly pretends to be IE7
		//jQuery(this).parent().find("li").css("position", "relative");
		});				
	//contraction with the minus sign (or vertical arrow)
	jQuery("p.ListMinus").click(function(){
		jQuery(this).hide();
		jQuery(this).prev("p.ListPlus").show();
		jQuery(this).prev("p.ListPlus").css("display", "inline");
		if(navigator.userAgent.match("MSIE 6")) //slideUp animation doesn't work in IE6.
		{
		    jQuery(this).parent().find("p.ListPlus").show();
		    jQuery(this).parent().find("p.ListMinus").hide();
		    jQuery(this).parent().find("p.ListMinus + span.bold ~ ul").hide();
		}
		else
		{
		    jQuery(this).parent().children("ul").slideUp("fast");
		}
	});
    jQuery("p#expand_all_clicker").click(function(){
        jQuery(document).showAllCommColl();
        jQuery(document).showSomeCommColl();
        jQuery("p#expand_all_clicker").hide();
        jQuery("p#collapse_all_clicker").show();
        });
    jQuery("p#collapse_all_clicker").click(function(){
        jQuery(document).hideAllCommColl();
        jQuery(document).hideSomeCommColl();
        jQuery("p#collapse_all_clicker").hide();
        jQuery("p#expand_all_clicker").show();
        jQuery("p#expand_all_clicker").css("display", "block");
        jQuery("p.ListPlus").css("display", "inline");
        });

	//*** Remember the state of the community browser	
	if (jQuery("#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser").length) {
		var rawCookie = null;
		//try to load cookie
		if (rawCookie = getCookie("expander")) {
			//parse cookie
			if (state = JSON.parse(rawCookie)) {
				//expand accordion for previously opened nodes
				jQuery.each(state,function(i,v) {
					jQuery("#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser a[href='"+v+"']").parent().siblings("p.ListPlus").click();
				});
			}
		}
		//on page exit: store state to cookie
		window.onbeforeunload = function () {
			var openNodes = [];
			jQuery("#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser ul").find("ul").each(function() {
				if (jQuery(this).is(":visible")) {
					openNodes.push(jQuery(this).siblings("span.bold").children("a").attr("href"));
				}
			});
			setCookie("expander",JSON.stringify(openNodes),10);
		};
	}
	
	//The metadata popups for ds-artifact-item-with-popup's
    jQuery("div.item_metadata_more").toggle(function(){
		jQuery(this).children(".item_more_text").hide();
		jQuery(this).children(".item_less_text").show();
		jQuery(this).next().slideDown();
	},function(){
		jQuery(this).children(".item_more_text").show();
		jQuery(this).children(".item_less_text").hide();
		jQuery(this).next().slideUp();
	});


	
	//slider for filter search
	jQuery("div#collapsible-filter-search-fields div.more_text").click(function(){
		jQuery("div#collapsible-filter-search-fields div.more_text").hide();
		jQuery("div#collapsible-filter-search-fields div.less_text").show();
		jQuery("div#collapsible-filter-search-fields-table-container").slideDown("fast");
		});
	jQuery("div#collapsible-filter-search-fields div.less_text").click(function(){
		jQuery("div#collapsible-filter-search-fields div.more_text").show();
		jQuery("div#collapsible-filter-search-fields div.less_text").hide();
		jQuery("div#collapsible-filter-search-fields-table-container").slideUp("fast");
		});



    //the about text slider	
	jQuery("p#rotating_header_more").click(function(){
		jQuery("p#rotating_header_para").slideUpShow("slow");
		jQuery("p#rotating_header_less").show();
		jQuery(this).hide();
		});
	
	jQuery("p#rotating_header_less").click(function(){
		jQuery("p#rotating_header_para").slideDownHide("slow");
		jQuery("p#rotating_header_more").show();
		jQuery(this).hide();
		});
		
		
	//the date range slider for filtersearch interface
    $maxYear = parseInt(jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option:eq(1)").val())
    $minYear = parseInt(jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option:last").val())
    
	jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted").after("<span id='startYear'>" + $minYear + "</span><span>&nbsp;-&nbsp;</span><span id='endYear'>" + $maxYear + "</span><div id='yearSlider'/>")
	jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted").hide();
	
	jQuery("div#yearSlider").slider({
		min: $minYear,
		max: $maxYear,
		range: true,
		values: [$minYear, $maxYear],
		step: 1,
		change: function(event, ui) 
			{ 
				//remove "selected" attribute from the options
				jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option").removeAttr("selected");
				
				//set "selected" attribute on each of the options from the option correponding to slider end value until slider start value is reached (keep in mind that due to the order of the options we're counting down)
			  	jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value="+jQuery(this).slider('values', 1)+"]").not("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value="+jQuery(this).slider('values', 0)+"]").nextUntil("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value="+jQuery(this).slider('values', 0)+"]").andSelf().attr("selected", "selected");
   				
   				//set "selected" attribute on the option corresponding to the the slider start value, which is explicitly missed by the nextUntil function.
   				jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value="+jQuery(this).slider('values', 0)+"]").attr("selected", "selected");
   				
   				jQuery("span#startYear").replaceWith("<span id='startYear'>"+jQuery(this).slider('values', 0)+"</span>");
   				jQuery("span#endYear").replaceWith("<span id='endYear'>"+jQuery(this).slider('values', 1)+"</span>");
   			}
		});
		
	jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value=" + $maxYear + "]").nextUntil("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value=" + $minYear + "]").andSelf().attr("selected", "selected");
   	jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value=" + $minYear + "]").attr("selected", "selected");
   	jQuery("#org_tdl_dspace_filtersearch_FilterSearch_field_etdsubmitted option[value='']").removeAttr("selected");
   	
   	
    // License step foo
   	
   	// Hide everything until the user makes a choice
   	jQuery(document).concealLicenseText();
   	jQuery("#aspect_submission_StepTransformer_list_submit-proxy-document").hide();
   	jQuery("#aspect_submission_StepTransformer_div_submit-license-inner #aspect_submission_StepTransformer_list_submit-review").hide();
   	
   	
   	jQuery("input[name='license-selector'][value='default']").click( function() {
   	    jQuery(document).concealLicenseText();
   	    jQuery("#aspect_submission_StepTransformer_div_submit-license-standard-text").show();
   	    jQuery("#aspect_submission_StepTransformer_div_submit-license-inner #aspect_submission_StepTransformer_list_submit-review").show();
   	});
   	
   	jQuery("input[name='license-selector'][value='proxy']").click( function() {
   	    jQuery(document).concealLicenseText();
   	    jQuery("#aspect_submission_StepTransformer_div_submit-license-proxy-text").show();
   	    jQuery("#aspect_submission_StepTransformer_list_submit-proxy-document").show();
   	    jQuery("#aspect_submission_StepTransformer_div_submit-license-inner #aspect_submission_StepTransformer_list_submit-review").show();
   	});
   	
   	jQuery("input[name='license-selector'][value='open']").click( function() {
   	    jQuery(document).concealLicenseText();
   	    jQuery("#aspect_submission_StepTransformer_div_submit-license-open-text").show();
   	    jQuery("#aspect_submission_StepTransformer_div_submit-license-inner #aspect_submission_StepTransformer_list_submit-review").show();
   	});
   	
   	
   	
	
	
});



jQuery.fn.extend({
  slideDownHide: function(speed){
  	return this.animate({height: "hide", top: "0px"}, speed);
  },
  slideUpShow: function(speed,callback){
  	return this.animate({height: "show", top: "-50px"}, speed);
  },
  hideAllCommColl: function(){
  	jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListPlus").show();
  	jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus").hide();
  	jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus + span.bold ~ ul").hide();
  },
  showAllCommColl: function(){
    jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus").show();
  	jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListPlus").hide();
  	jQuery("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus + span.bold ~ ul").show();
  },
  hideSomeCommColl: function(){
    jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p.ListPlus").show();
  	jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p.ListMinus").hide();
  	jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p.ListMinus + span.bold ~ ul").hide();
  },
  showSomeCommColl: function(){
    jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p.ListMinus").show();
  	jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p.ListPlus").hide();
  	jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view p.ListMinus + span.bold ~ ul").show();
  },
  hideCommCollInCommViewTo2ndLevel: function(){
    jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view > ul.ds-artifact-list > li > ul.ds-artifact-list p.ListPlus").show();
    jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view > ul.ds-artifact-list > li > ul.ds-artifact-list p.ListMinus").hide();
    jQuery("div#aspect_artifactbrowser_CommunityViewer_div_community-view > ul.ds-artifact-list > li > ul.ds-artifact-list p.ListMinus + span.bold ~ ul").hide();
  },
  concealLicenseText: function() {
    jQuery("#aspect_submission_StepTransformer_list_submit-proxy-document").hide();
    jQuery("#aspect_submission_StepTransformer_div_submit-license-standard-text").hide();
   	jQuery("#aspect_submission_StepTransformer_div_submit-license-proxy-text").hide();
   	jQuery("#aspect_submission_StepTransformer_div_submit-license-open-text").hide();
  }/*,
  //was used for the unpopular hover-sliders on the left menu
  myReBind: function(){
      jQuery("div.menuslider h3.open").unbind("mouseover");
      
      jQuery("div.menuslider h3.closed").bind("mouseover",function(){
	    //hide any open sub-menu
	    jQuery("h3.open").next().slideUp("slow");
		jQuery("h3.open").find("img.menu_nav_icon_open").hide();
		jQuery("h3.open").find("img.menu_nav_icon_closed").show();
	    jQuery("h3.open").addClass("closed");
	    jQuery("h3.open").removeClass("open");
	    
	    //show this sub-menu
	    jQuery(this).next("div.ds-option-set").slideDown("slow");
	    jQuery(this).addClass("open");
	    jQuery(this).removeClass("closed");
		jQuery(this).find("img.menu_nav_icon_open").show();
		jQuery(this).find("img.menu_nav_icon_closed").hide();
		
		jQuery(document).myReBind();
	});
  }*/
});


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
	jQuery("#edu_tamu_metadatatreebrowser_BrowseOverview_div_metadata-tree-browser-overview").prepend("<a class=\"expand-all\" href=\"javascript: void(0);\">Expand All</a>");
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
