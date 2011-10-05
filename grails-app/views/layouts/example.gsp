<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <meta http-equiv="X-UA-Compatible" content="IE=7"/>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

    <title><g:layoutTitle default="Grails"/></title>

    %{--<style type="text/css" media="all">--}%
    %{--@import url("css/style.css");--}%
    %{--@import url("css/jquery.wysiwyg.css");--}%
    %{--@import url("css/facebox.css");--}%
    %{--@import url("css/visualize.css");--}%
    %{--@import url("css/date_input.css");--}%
    %{--</style>--}%

    <link rel="stylesheet" href="${resource(dir: 'css', file: 'style.css')}"/>

    %{--main jquery js file--}%
    <g:javascript library="jquery" plugin="jquery"/>

    %{-- adminus stuff --}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.img.preload.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.visualize.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.tablesorter.min.jsvis')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'facebox.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.select_skin.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.filestyle.mini.js')}"></script>


    %{--jquery edit in place plugin--}%
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.editinplace.js')}"></script>


    %{--application-specific scripts--}%
    <g:javascript library="application"/>

    <g:layoutHead/>

</head>


<body>

<div id="hld">

<div class="wrapper"><!-- wrapper begins -->



<div id="header">
    <div class="hdrl"></div>

    <div class="hdrr"></div>

    <h1><a href="#">AfterParty</a></h1>

    <ul id="nav">

        <li class="active"><a href="#">Pages</a>
            <ul>
                <li><a href="#">List pages</a></li>
                <li><a href="#">Add page</a></li>
                <li><a href="#">More actions</a>
                    <ul>
                        <li><a href="#">Some action</a></li>
                        <li><a href="#">Some action</a></li>
                        <li><a href="#">Some action</a>
                            <ul>
                                <li><a href="#">Some action</a></li>
                                <li><a href="#">Some action</a></li>
                                <li><a href="#">Some action</a></li>
                            </ul>
                        </li>
                    </ul>
                </li>
            </ul>
        </li>

        <g:include controller="nav" action="show"/>


        <g:include controller="nav" action="showStudies"/>


        <li><a href="#">Media</a>
            <ul>
                <li><a href="#">List media</a></li>
                <li><a href="#">Add media</a></li>
                <li><a href="#">Something else</a></li>
            </ul>
        </li>
        <li><a href="#">Users</a></li>
    </ul>

    <p class="user">Hello, <a href="#">John</a> | <a href="index.html">Logout</a></p>
</div>        <!-- #header ends -->


<g:layoutBody/>






<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Web stats</h2>

        <ul class="tabs">
            <li><a href="#days">Per days</a></li>
            <li><a href="#months">Per months</a></li>
        </ul>
    </div>        <!-- .block_head ends -->



    <div class="block_content tab_content" id="days">

        <table class="stats" rel="line" cellpadding="0" cellspacing="0" width="100%">

            <thead>
            <tr>
                <td>&nbsp;</td>
                <th scope="col">01.03</th>
                <th scope="col">02.03</th>
                <th scope="col">03.03</th>
                <th scope="col">04.03</th>
                <th scope="col">05.03</th>
                <th scope="col">06.03</th>
                <th scope="col">07.03</th>
                <th scope="col">08.03</th>
                <th scope="col">09.03</th>
                <th scope="col">10.03</th>
                <th scope="col">11.03</th>
                <th scope="col">12.03</th>
                <th scope="col">13.03</th>
                <th scope="col">14.03</th>
            </tr>
            </thead>

            <tbody>
            <tr>
                <th>Page views</th>
                <td>50</td>
                <td>90</td>
                <td>40</td>
                <td>120</td>
                <td>180</td>
                <td>280</td>
                <td>320</td>
                <td>220</td>
                <td>100</td>
                <td>120</td>
                <td>40</td>
                <td>70</td>
                <td>20</td>
                <td>60</td>
            </tr>

            <tr>
                <th>Unique visitors</th>
                <td>30</td>
                <td>50</td>
                <td>15</td>
                <td>90</td>
                <td>140</td>
                <td>160</td>
                <td>230</td>
                <td>170</td>
                <td>50</td>
                <td>90</td>
                <td>30</td>
                <td>50</td>
                <td>10</td>
                <td>40</td>
            </tr>
            </tbody>
        </table>

    </div>        <!-- .block_content ends -->





    <div class="block_content tab_content" id="months">

        <table class="stats" rel="bar" cellpadding="0" cellspacing="0" width="100%">

            <thead>
            <tr>
                <td>&nbsp;</td>
                <th scope="col">JAN</th>
                <th scope="col">FEB</th>
                <th scope="col">MAR</th>
                <th scope="col">APR</th>
                <th scope="col">MAY</th>
                <th scope="col">JUN</th>
                <th scope="col">JUL</th>
                <th scope="col">AUG</th>
                <th scope="col">SEP</th>
                <th scope="col">OCT</th>
                <th scope="col">NOV</th>
                <th scope="col">DEC</th>
                <th scope="col">JAN</th>
                <th scope="col">FEB</th>
            </tr>
            </thead>

            <tbody>
            <tr>
                <th scope="row">Page views</th>
                <td>1800</td>
                <td>900</td>
                <td>700</td>
                <td>1200</td>
                <td>600</td>
                <td>2800</td>
                <td>3200</td>
                <td>500</td>
                <td>2200</td>
                <td>1000</td>
                <td>1200</td>
                <td>700</td>
                <td>650</td>
                <td>800</td>
            </tr>

            <tr>
                <th scope="row">Unique visitors</th>
                <td>1600</td>
                <td>650</td>
                <td>550</td>
                <td>900</td>
                <td>500</td>
                <td>2300</td>
                <td>2700</td>
                <td>350</td>
                <td>1700</td>
                <td>600</td>
                <td>1000</td>
                <td>500</td>
                <td>400</td>
                <td>700</td>
            </tr>
            </tbody>
        </table>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>        <!-- .block ends -->














<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Pages</h2>

        <ul>
            <li><a href="#">Edit pages</a></li>
            <li><a href="#">Add page</a></li>
        </ul>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <form action="" method="post">

            <table cellpadding="0" cellspacing="0" width="100%" class="sortable">

                <thead>
                <tr>
                    <th width="10"><input type="checkbox" class="check_all"/></th>
                    <th>Page title</th>
                    <th>Status</th>
                    <th>Date created</th>
                    <th>Author</th>
                    <td>&nbsp;</td>
                </tr>
                </thead>

                <tbody>
                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">Aorem ipsum dolor</a></td>
                    <td>Published</td>
                    <td>20.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">Zn ac libero nunc, vel congue neque</a></td>
                    <td>Published</td>
                    <td>18.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">Borem ipsum dolor</a></td>
                    <td>Published</td>
                    <td>20.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">Aaa In ac libero nunc, vel congue neque</a></td>
                    <td>Published</td>
                    <td>18.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">Lorem ipsum dolor</a></td>
                    <td>Published</td>
                    <td>20.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">In ac libero nunc, vel congue neque</a></td>
                    <td>Published</td>
                    <td>18.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">Lorem ipsum dolor</a></td>
                    <td>Published</td>
                    <td>20.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>

                <tr>
                    <td><input type="checkbox"/></td>
                    <td><a href="#">In ac libero nunc, vel congue neque</a></td>
                    <td>Published</td>
                    <td>08.03.2010</td>
                    <td><a href="#">John Doe</a></td>
                    <td class="delete"><a href="#">Delete</a></td>
                </tr>
                </tbody>

            </table>


            <div class="tableactions">
                <select>
                    <option>Actions</option>
                    <option>Delete</option>
                    <option>Edit</option>
                </select>

                <input type="submit" class="submit tiny" value="Apply to selected"/>
            </div>        <!-- .tableactions ends -->



            <div class="pagination right">
                <a href="#">&laquo;</a>
                <a href="#" class="active">1</a>
                <a href="#">2</a>
                <a href="#">3</a>
                <a href="#">4</a>
                <a href="#">5</a>
                <a href="#">6</a>
                <a href="#">&raquo;</a>
            </div>        <!-- .pagination ends -->

        </form>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>
</div>        <!-- .block ends -->




















<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Forms</h2>

        <form action="" method="post">
            <input type="text" class="text" value="Search"/>
        </form>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <p class="breadcrumb"><a href="#">Parent page</a> &raquo; <a
                href="#">Sub page</a> &raquo; <strong>Form page</strong> (breadcrumb)</p>

        <div class="message errormsg"><p>An error message goes here</p></div>

        <div class="message success"><p>A success message goes here</p></div>

        <div class="message info"><p>An informative message goes here</p></div>

        <div class="message warning"><p>A warning message goes here</p></div>


        <form action="" method="post">
            <p>
                <label>Small input label:</label><br/>
                <input type="text" class="text small"/>
                <span class="note">*A note</span>
            </p>

            <p>
                <label>Medium input label:</label><br/>
                <input type="text" class="text medium error"/>
                <span class="note error">Error!</span>
            </p>

            <p>
                <label>Big input label:</label><br/>
                <input type="text" class="text big"/>
            </p>

            <p>
                <label>Textarea label:</label><br/>
                <textarea class="wysiwyg"></textarea>
            </p>

            <p>
                <label>Starting date:</label>
                <input type="text" class="text date_picker"/>
                &nbsp;&nbsp;
                <label>Ending date:</label>
                <input type="text" class="text date_picker"/>
            </p>


            <p><label>Select label:</label> <br/>

                <select class="styled">
                    <optgroup label="Group 1">
                        <option>Option one</option>
                        <option>Option two</option>
                        <option>Option three</option>
                    </optgroup>

                    <optgroup label="Group 2">
                        <option>Option one</option>
                        <option>Option two</option>
                        <option>Option three</option>
                    </optgroup>

                    <optgroup label="Group 3">
                        <option>Option one</option>
                        <option>Option two</option>
                        <option>Option three</option>
                    </optgroup>
                </select></p>


            <p class="fileupload">
                <label>File input label:</label><br/>
                <input type="file" id="fileupload"/>
                <span id="uploadmsg">Max size 3Mb</span>
            </p>

            <p>
                <input type="checkbox" class="checkbox" checked="checked" id="cbdemo1"/> <label
                    for="cbdemo1">Checkbox label</label>
                <input type="checkbox" class="checkbox" id="cbdemo2"/> <label for="cbdemo2">Checkbox label</label>
            </p>

            <p><input type="radio" checked="checked" class="radio"/> <label>Radio button label</label></p>

            <hr/>

            <p>
                <input type="submit" class="submit small" value="Submit"/>
                <input type="submit" class="submit mid" value="Long submit"/>
                <input type="submit" class="submit long" value="Even longer submit"/>
            </p>
        </form>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>        <!-- .block ends -->
















<div class="block small left">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Left column</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <form action="" method="post">
            <p>
                <label>Small input label:</label><br/>
                <input type="text" class="text"/>
            </p>

            <p>
                <label>Text area label:</label><br/>
                <textarea rows="5" cols="50"></textarea>
            </p>

            <p>Praesent justo risus, suscipit eget volutpat ac, fermentum ac.</p>

            <p><input type="submit" class="submit small" value="Submit"/></p>
        </form>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>        <!-- .block.small.left ends -->















<div class="block small right">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Right column</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <h2>Headline</h2>

        <p>Quisque felis nunc, <a href="demo"
                                  rel="facebox">open modal</a> lacinia at, viverra sit amet ligula. Praesent justo risus, suscipit eget volutpat ac, fermentum ac massa. Sed at justo velit. Maecenas dapibus sem nec quam cursus.
        </p>


        <h3>Headline</h3>
        <ul>
            <li>Proin volutpat</li>
            <li>Praesent justo risus, suscipit eget</li>
            <li>Morbi facilisis felis ac sapien</li>
        </ul>


        <h4>Headline</h4>
        <ol>
            <li>Proin volutpat</li>
            <li>Praesent justo risus, suscipit eget</li>
            <li>Morbi facilisis felis ac sapien</li>
        </ol>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>        <!-- .block.small.right ends -->















<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Big text box with tabs</h2>

        <ul class="tabs">
            <li><a href="#tab1">Tab 1</a></li>
            <li><a href="#tab2">Tab 2</a></li>
        </ul>
    </div>        <!-- .block_head ends -->



    <div class="block_content tab_content" id="tab1">

        <h3>This is the first tab</h3>

        <p>Maecenas facilisis interdum rhoncus. Sed laoreet vulputate lacus sit amet aliquam. Praesent vitae sapien orci. Mauris nec purus in mi accumsan convallis non et lorem. Nunc tincidunt consequat risus, ac tincidunt nibh hendrerit at. Nullam sit amet nisi eget magna lacinia ullamcorper non sed sem. Ut ornare consequat commodo. Donec vitae justo risus. Nulla ornare posuere egestas. Nulla varius purus quis lacus placerat tincidunt.</p>

    </div>        <!-- .block_content ends -->



    <div class="block_content tab_content" id="tab2">

        <h3>This is the second tab</h3>

        <p>Praesent vitae sapien orci. Mauris nec purus in mi accumsan convallis non et lorem. Nunc tincidunt consequat risus, ac tincidunt nibh hendrerit at. Nullam sit amet nisi eget magna lacinia ullamcorper non sed sem. Ut ornare consequat commodo. Donec vitae justo risus. Nulla ornare posuere egestas. Nulla varius purus quis lacus placerat tincidunt. Maecenas facilisis interdum rhoncus. Sed laoreet vulputate lacus sit amet aliquam.</p>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>        <!-- .block ends -->















<div class="block withsidebar">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Big text box with sidebar</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <div class="sidebar">
            <ul class="sidemenu">
                <li><a href="#sb1">Subpage one</a></li>
                <li><a href="#sb2">Subpage two</a></li>
                <li><a href="#sb3">Subpage three</a></li>
            </ul>

            <p><strong>Some notification text.</strong> Donec hendrerit porttitor felis, id semper eros lobortis sed. Class aptent taciti sociosqu ad litora.
            </p>
        </div>        <!-- .sidebar ends -->

        <div class="sidebar_content" id="sb1">
            <h3>Sidebar content 1</h3>

            <p>Aenean facilisis ligula eget orci adipiscing varius. Curabitur sem ligula, egestas vel bibendum sed, sodales eu nulla. Vestibulum luctus aliquam feugiat. Donec porta interdum placerat. Donec velit enim, porta vitae euismod ut, fermentum eu felis. Morbi aliquet, libero vel gravida facilisis, enim risus consequat tellus, vitae luctus est diam non nisi. Vivamus eget leo sit amet neque ultricies blandit. Sed tristique erat a sem ullamcorper tempor. Curabitur turpis lorem, semper et pharetra in, scelerisque in magna. Ut at tortor sed diam mattis rhoncus vel eget turpis. Praesent id diam velit, ullamcorper semper augue. Curabitur at orci tellus, sed tincidunt enim. Vivamus sed dolor vitae purus dignissim luctus quis sed nunc. Sed urna enim, auctor sit amet volutpat ut, porta sed leo. Integer dictum molestie elementum. Nullam dapibus tempus enim, id tincidunt arcu elementum varius.</p>

            <p>Integer malesuada posuere nibh, ac commodo eros dictum eget. Maecenas vel urna ac sapien posuere tincidunt vel non metus. Morbi accumsan lectus at ante scelerisque molestie. Pellentesque in quam arcu, in lacinia orci. Sed blandit, neque sed ullamcorper lacinia, velit lectus lacinia lorem, id gravida sem arcu vel purus. Aenean tellus massa, elementum id condimentum ut, tempus ac dui. Integer consectetur neque placerat leo adipiscing iaculis. Vivamus tempor dui eu augue malesuada dignissim. In tempor odio eu augue ultricies ut hendrerit.</p>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb2">
            <h3>Sidebar content 2</h3>

            <p>Aenean facilisis ligula eget orci adipiscing varius. Curabitur sem ligula, egestas vel bibendum sed, sodales eu nulla. Vestibulum luctus aliquam feugiat. Donec porta interdum placerat. Donec velit enim, porta vitae euismod ut, fermentum eu felis. Morbi aliquet, libero vel gravida facilisis, enim risus consequat tellus, vitae luctus est diam non nisi. Vivamus eget leo sit amet neque ultricies blandit. Sed tristique erat a sem ullamcorper tempor. Curabitur turpis lorem, semper et pharetra in, scelerisque in magna. Ut at tortor sed diam mattis rhoncus vel eget turpis. Praesent id diam velit, ullamcorper semper augue. Curabitur at orci tellus, sed tincidunt enim. Vivamus sed dolor vitae purus dignissim luctus quis sed nunc. Sed urna enim, auctor sit amet volutpat ut, porta sed leo. Integer dictum molestie elementum. Nullam dapibus tempus enim, id tincidunt arcu elementum varius.</p>

            <p>Integer malesuada posuere nibh, ac commodo eros dictum eget. Maecenas vel urna ac sapien posuere tincidunt vel non metus. Morbi accumsan lectus at ante scelerisque molestie. Pellentesque in quam arcu, in lacinia orci. Sed blandit, neque sed ullamcorper lacinia, velit lectus lacinia lorem, id gravida sem arcu vel purus. Aenean tellus massa, elementum id condimentum ut, tempus ac dui. Integer consectetur neque placerat leo adipiscing iaculis. Vivamus tempor dui eu augue malesuada dignissim. In tempor odio eu augue ultricies ut hendrerit.</p>
        </div>        <!-- .sidebar_content ends -->


        <div class="sidebar_content" id="sb3">
            <h3>Sidebar content 3</h3>

            <p>Aenean facilisis ligula eget orci adipiscing varius. Curabitur sem ligula, egestas vel bibendum sed, sodales eu nulla. Vestibulum luctus aliquam feugiat. Donec porta interdum placerat. Donec velit enim, porta vitae euismod ut, fermentum eu felis. Morbi aliquet, libero vel gravida facilisis, enim risus consequat tellus, vitae luctus est diam non nisi. Vivamus eget leo sit amet neque ultricies blandit. Sed tristique erat a sem ullamcorper tempor. Curabitur turpis lorem, semper et pharetra in, scelerisque in magna. Ut at tortor sed diam mattis rhoncus vel eget turpis. Praesent id diam velit, ullamcorper semper augue. Curabitur at orci tellus, sed tincidunt enim. Vivamus sed dolor vitae purus dignissim luctus quis sed nunc. Sed urna enim, auctor sit amet volutpat ut, porta sed leo. Integer dictum molestie elementum. Nullam dapibus tempus enim, id tincidunt arcu elementum varius.</p>

            <p>Integer malesuada posuere nibh, ac commodo eros dictum eget. Maecenas vel urna ac sapien posuere tincidunt vel non metus. Morbi accumsan lectus at ante scelerisque molestie. Pellentesque in quam arcu, in lacinia orci. Sed blandit, neque sed ullamcorper lacinia, velit lectus lacinia lorem, id gravida sem arcu vel purus. Aenean tellus massa, elementum id condimentum ut, tempus ac dui. Integer consectetur neque placerat leo adipiscing iaculis. Vivamus tempor dui eu augue malesuada dignissim. In tempor odio eu augue ultricies ut hendrerit.</p>
        </div>        <!-- .sidebar_content ends -->

    </div>        <!-- .block_content ends -->


    <div class="bendl"></div>

    <div class="bendr"></div>

</div>        <!-- .block ends -->

















<div class="block">

    <div class="block_head">
        <div class="bheadl"></div>

        <div class="bheadr"></div>

        <h2>Images</h2>
    </div>        <!-- .block_head ends -->



    <div class="block_content">

        <ul class="imglist">
            <li>
                <img src="images/thumb1.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2646/4240549111_fcb8c478fe.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb2.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2493/4241304118_98ff39d232.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb3.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2662/4241252138_b9c755e9ca.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb1.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2646/4240549111_fcb8c478fe.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb2.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2493/4241304118_98ff39d232.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb3.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2662/4241252138_b9c755e9ca.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb1.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2646/4240549111_fcb8c478fe.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb2.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2493/4241304118_98ff39d232.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb3.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2662/4241252138_b9c755e9ca.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb1.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2646/4240549111_fcb8c478fe.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb2.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2493/4241304118_98ff39d232.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#">Delete</a></li>
                </ul>
            </li>

            <li>
                <img src="images/thumb3.jpg" alt=""/>
                <ul>
                    <li class="view"><a href="http://farm3.static.flickr.com/2662/4241252138_b9c755e9ca.jpg"
                                        rel="facebox">View</a></li>
                    <li class="delete"><a href="#deleted">Delete</a></li>
                </ul>
            </li>

        </ul>

    </div>        <!-- .block_content ends -->

    <div class="bendl"></div>

    <div class="bendr"></div>

</div>        <!-- .leftcol ends -->












<div id="footer">

    <p class="left"><a href="#">YourWebsite.com</a></p>

    <p class="right">powered by <a
            href="http://themeforest.net/item/adminus-beautiful-admin-panel-interface/94668?ref=enstyled">Adminus</a> v1.4
    </p>

</div>

</div>                        <!-- wrapper ends -->

</div>        <!-- #hld ends -->


%{--<!--[if IE]><script type="text/javascript" src="js/excanvas.js"></script><![endif]-->--}%
%{--<script type="text/javascript" src="js/jquery.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.img.preload.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.filestyle.mini.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.wysiwyg.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.date_input.pack.js"></script>--}%
%{--<script type="text/javascript" src="js/facebox.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.visualize.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.visualize.tooltip.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.select_skin.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.tablesorter.min.js"></script>--}%
%{--<script type="text/javascript" src="js/ajaxupload.js"></script>--}%
%{--<script type="text/javascript" src="js/jquery.pngfix.js"></script>--}%
%{--<script type="text/javascript" src="js/custom.js"></script>--}%

%{--adminus scripts--}%
%{--<script type="text/javascript" src="${resource(dir: 'js', file: 'custom.js')}"></script>--}%
%{--<script type="text/javascript" src="${resource(dir: 'js', file: '')}"></script>--}%

</body>
</html>