$(document).ready(function(){

	//alert("Render mode: "+ document.compatMode);
	
	//eliminate ListPlus and ListMinus for the community/collection hierarchy when there is no ul contained in the li.
	//replace them with spacers to make the hierarchy look flush
	//div#aspect_artifactbrowser_CommunityViewer_div_community-view
	//div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser	
	$("div.ds-static-div li:not(:has(ul))").children("p.ListPlus").after("<p class=\"ListEmpty\"></p>");
	$("div.ds-static-div li:not(:has(ul))").children("p.ListMinus, p.ListPlus").remove();
	
	//close the community/collection lists by default
	$(document).hideAllCommColl();
		
	//show the Expand All link
	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p#expand_all_clicker").show();
	
		
	//community/collection hierarchy
	//expansion with the plus sign (or horizontal arrow)
	$("p.ListPlus").click(function(){
		$(this).hide();
		$(this).next("p.ListMinus").show();
		$(this).parent().children("ul").slideDown("fast");
		});				
	//contraction with the minus sign (or vertical arrow)
	$("p.ListMinus").click(function(){
		$(this).hide();
		$(this).prev("p.ListPlus").show();
		$(this).parent().children("ul").slideUp("fast");
		});
    $("p#expand_all_clicker").click(function(){
        $(document).showAllCommColl();
        $("p#expand_all_clicker").hide();
        $("p#collapse_all_clicker").show();
        });
    $("p#collapse_all_clicker").click(function(){
        $(document).hideAllCommColl();
        $("p#collapse_all_clicker").hide();
        $("p#expand_all_clicker").show();
        });
	
	
	
	
	
	//The metadata popups for ds-artifact-item-with-popup's
    $("div.item_metadata_more").toggle(function(){
		$(this).children(".item_more_text").hide();
		$(this).children(".item_less_text").show();
		$(this).next().slideDown();
	},function(){
		$(this).children(".item_more_text").show();
		$(this).children(".item_less_text").hide();
		$(this).next().slideUp();
	});


	
	//slider for filter search
	$("div#collapsible-filter-search-fields div.more_text").click(function(){
		$("div#collapsible-filter-search-fields div.more_text").hide();
		$("div#collapsible-filter-search-fields div.less_text").show();
		$("div#collapsible-filter-search-fields-table-container").slideDown("fast");
		});
	$("div#collapsible-filter-search-fields div.less_text").click(function(){
		$("div#collapsible-filter-search-fields div.more_text").show();
		$("div#collapsible-filter-search-fields div.less_text").hide();
		$("div#collapsible-filter-search-fields-table-container").slideUp("fast");
		});



    //grab the front page news header and text, and move them to the bottom of the body.
    $("div#front-page-image-wrapper + h1.ds-div-head").clone().appendTo("div#ds-body");
    $("div#front-page-image-wrapper + h1.ds-div-head + div#file_news_div_news").clone().appendTo("div#ds-body");
    
    $("div#front-page-image-wrapper + h1.ds-div-head").remove();
    $("div#front-page-image-wrapper + div#file_news_div_news").remove();
    
    

	//eliminate the expansion clickers when they appear in the context of the front page
	$("div#front-page-image-wrapper + h1.ds-div-head + div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p#expand_all_clicker, form#aspect_artifactbrowser_FrontPageSearch_div_front-page-search + h1.ds-div-head + div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p#collapse_all_clicker").remove();


});



jQuery.fn.extend({
 
  hideAllCommColl: function(){
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListPlus").show();
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus").hide();
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus + span.bold ~ ul").hide();
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser span.bold + p.ListPlus ~ ul").hide();
  },
  showAllCommColl: function(){
    $("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus").show();
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListPlus").hide();
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ListMinus + span.bold ~ ul").show();
  	$("div#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser span.bold + p.ListPlus ~ ul").show();
  }
});