<html>
  <head>
    <meta name='layout' content='main'/>
    <title><g:message code="springSecurity.login.title"/></title>
    <style type='text/css' media='screen'>
    body {
      background-color : #eee;
    }

    .form-signin {
      max-width : 330px;
      padding   : 15px;
      margin    : 0 auto;
    }

    .form-signin .form-signin-heading,
    .form-signin .checkbox {
      margin-bottom : 10px;
    }

    .form-signin .checkbox {
      font-weight : normal;
    }

    .form-signin .form-control {
      position           : relative;
      height             : auto;
      -webkit-box-sizing : border-box;
      -moz-box-sizing    : border-box;
      box-sizing         : border-box;
      padding            : 10px;
      font-size          : 16px;
    }

    .form-signin .form-control:focus {
      z-index : 2;
    }

    .form-signin input[type="text"] {
      margin-bottom              : -1px;
      border-bottom-right-radius : 0;
      border-bottom-left-radius  : 0;
    }

    .form-signin input[type="password"] {
      margin-bottom           : 10px;
      border-top-left-radius  : 0;
      border-top-right-radius : 0;
    }
    </style>
  </head>

  <body>

    <form class="form-signin" role="form" autocomplete='off' action='${postUrl}' method='POST' id='loginForm'>
      <g:if test='${flash.message}'>
        <div class="alert alert-danger" role="alert">${flash.message}</div>
      </g:if>
      <h2 class="form-signin-heading"><g:message code="springSecurity.login.header"/></h2>
      <input type="text" class="form-control" placeholder="${g.message(code: 'springSecurity.login.username.label')}"
             required autofocus name='j_username' id='username'>
      <input type="password" class="form-control"
             placeholder="${g.message(code: 'springSecurity.login.password.label')}"
             required name='j_password' id='password'>
      <label class="checkbox">
        <input type='checkbox' class='chk' name='${rememberMeParameter}' id='remember_me'
               <g:if test='${hasCookie}'>checked='checked'</g:if>/>
        <g:message code="springSecurity.login.remember.me.label"/>
      </label>
      <button class="btn btn-lg btn-primary btn-block" type="submit" id="submit">
        ${message(code: "springSecurity.login.button")}</button>
    </form>

    <script type='text/javascript'>
      <!--
      (function () {
        document.forms['loginForm'].elements['j_username'].focus();
      })();
      // -->
    </script>
  </body>
</html>
