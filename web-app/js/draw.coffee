printme = (data) ->
  alert(data.length)

drawContig = (contigId) ->
  alert('drawing contig with ' + contigId)
  paper = Raphael('coffeescript_annotation', 1000, 1000)
  $.get('/contig/showJSON/7764', printme)