
function showSearchBox() {
    $('.doSomethingButton').slideUp('slow');
    $('#blastForm').hide();
    $('#searchForm').slideDown('slow');

    return false;
}

function showBLASTBox() {
    $('.doSomethingButton').slideUp('slow');
    $('#searchForm').hide();
    $('#blastForm').slideDown('slow');
    return false;
}

function submitSearchForm() {
    $('#contigSetForm').attr('action', '/contigSet/searchContigSets');
}
function submitBLASTForm() {
    $('#contigSetForm').attr('action', '/contigSet/blastAgainstContigSets');
}
function submitCompare() {
    $('#contigSetForm').attr('action', '/contigSet/compareContigSets');
}
function submitDownload() {
    $('#contigSetForm').attr('action', '/contigSet/download');
}




function doCreate(idList, studyId) {
    var name = prompt("Enter a name for the new contig set", "");
    $.post(
            'createContigSetAJAX',
            {
                idList : idList.join(','),
                studyId : studyId,
                setName: name
            },
            function(data) {
                window.location = '../contigSet/compareContigSets/?idList=  ' + data;  // redirect to view the new contig set
            }
    );
}


// function to periodically update the progress of jobs running in the background
function updateJobStatus() {

    // make the http request to get the job info
    jQuery.ajax({
        type:'POST',
        url:'/backgroundJob/listAjax',
        success:function(data, textStatus) {
            // on success, update the joblist div
            jQuery('#jobList').html(data);
            //see if we have any jobs still running by checking for the existence of an element with the class runningJob
            if ($('.runningJob').length > 0) {
                //if we still have running jobs, set a timeout to check again in 3 seconds
                t = setTimeout("updateJobStatus()", 1000);
            }
        },
        // on error, don't do anything
        error:function(XMLHttpRequest, textStatus, errorThrown) {
        }
    });
}


function setUpEditInPlace(domainId, updateUrl, className) {
// loop through each element that has the class 'edit_in_place'
    $(".edit_in_place").each(function(index) {
//                run the editInPlace() method on it
        $(this).editInPlace({
            textarea_cols : 100,
            textarea_rows: 1,
            text_size:5,
//                    the url will always be the same
            url: updateUrl,
//                    construct the parameters by grabbing the name attribute of the element (for the property name) and the id
            params: "className=" + className + "&fieldName=" + $(this).attr('name') + "&id=" + domainId

        });
    });

}

