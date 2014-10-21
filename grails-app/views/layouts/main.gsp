<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="Grails"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
    <link rel="apple-touch-icon" href="${assetPath(src: 'apple-touch-icon.png')}">
    <link rel="apple-touch-icon" sizes="114x114" href="${assetPath(src: 'apple-touch-icon-retina.png')}">
    <asset:stylesheet src="application.css"/>
    <asset:javascript src="application.js"/>
    <g:layoutHead/>
  </head>

  <body>
    <g:set var="pageUri" value="${request.forwardURI - g.createLink(uri: '/')}"/>
    <nav class="navbar navbar-default" role="navigation">
      <div class="container">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                  data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="${g.createLink(uri: '/')}">MaaS</a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
          <ul class="nav navbar-nav">

            <li class="${pageUri == '' ? 'active' : ''}"><a href="${g.createLink(uri: '/')}">Home</a></li>
            <li class="${pageUri == '/about' ? 'active' : ''}"><a href="${g.createLink(uri: '/about')}">About</a></li>
            <li class="${params.controller == 'api' ? 'active' : ''}"><a href="${g.createLink(controller: 'api')}">API</a></li>
            <li class="${params.controller == 'status' ? 'active' : ''}"><a href="${g.createLink(controller: 'status')}">Status</a></li>
            <li class="${params.controller == 'console' ? 'active' : ''}"><a href="${g.createLink(controller: 'console')}">Console</a></li>
          </ul>

          <ul class="nav navbar-nav navbar-right">
            <sec:ifLoggedIn>
              <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                  <sec:username/>
                  <span class="caret"></span></a>
                <ul class="dropdown-menu" role="menu">
                  <!--
                  <li><a href="#">Action</a></li>
                  <li><a href="#">Another action</a></li>
                  <li><a href="#">Something else here</a></li> -->
                  <li class="divider"></li>
                  <li><a href="${g.createLink(controller: 'logout')}">Logout</a></li>
                </ul>
              </li>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
              <li><a href="${g.createLink(controller: 'login')}">Login</a></li>
            </sec:ifNotLoggedIn>

          </ul>
        </div><!-- /.navbar-collapse -->
      </div><!-- /.container-fluid -->
    </nav>

    <div class="container">
      <g:layoutBody/>
      <div class="footer" role="contentinfo"></div>
    </div>

    <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt"
                                                                       default="Loading&hellip;"/></div>
  </body>
</html>
