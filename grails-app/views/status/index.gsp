<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title>Status</title>
    <link rel="stylesheet" href="http://c3js.org/css/c3-f750e4d4.css"/>
    <meta name="layout" content="main"/>
  </head>

  <body>
    <div id="gauges">

    </div>

    <div id="timers">

    </div>
    <script src="//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js"></script>
    <script src="http://c3js.org/js/c3.min-b4e07444.js"></script>
    <script src="http://c3js.org/js/d3.min-3bff8220.js"></script>
    <script>
      var charts = {};
      function buildGauge(name, value) {
        if (!charts[name]) {
          $('div#gauges').append('<div id="' + name + '"></div>');
          charts[name] = c3.generate({
            bindto: '#' + name,
            data: {
              type: 'gauge',
              columns: [
                [name, value.value]
              ]
            },
            gauge: {
              label: {
                format: function (value, ratio) {
                  console.log(ratio);
                  return value;
                },
                show: false // to turn off the min/max labels.
              }
            },
            color: {
              pattern: ['#FF0000', '#F97600', '#F6C600', '#60B044'], // the three color levels for the percentage values.
              threshold: {
                values: [30, 60, 90, 100]
              }
            }
          });
        }
        charts[name].load({
          columns: [
            [name, value.value]
          ]
        });
      }
      function updateData() {
        setTimeout(updateData, 3000);
        $.getJSON('${g.createLink(uri: '/metrics/metrics')}', function (json) {
          console.log(json);
          $.each(json.gauges, function (name, value) { // gauges
            buildGauge(name.replace('$', '_').replace(/\./g, '_'), value);
          }); // gauges
        });
      }

      updateData();
    </script>
  </body>
</html>