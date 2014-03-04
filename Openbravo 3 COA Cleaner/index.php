<script src='js/jquery.js' ></script>
<script>
	$(function()
	{
		$("#aEncode").click(function(){
			var val = $('[name=toEncode]').val()			
			decode(val)
		})
	})
	
	decode = function(value)
	{					
		var encodeVal1 = value.split(/\n/g);				
		$.each(encodeVal1, function(i, v)
		{
			var encodeVal2 = v.split(",");			
			var finalValue = "";			
			if(encodeVal2 != "")
			{
				$.each(encodeVal2, function(index, value)
				{									
					if(value != '' && isNaN(value))
					{
						if(index == encodeVal2.length - 1)
						{
							finalValue += '<span>"' + value + '"</span>';
						}
						else
						{ 
							finalValue += '<span>"' + value + '",</span>';						
						}
					}
					else{					
						if(index == encodeVal2.length - 1)
						{ 
							finalValue += "<span>" + value + '</span>';
						}
						else
						{
							finalValue += "<span>" + value + ',</span>';	
						}
					}				
				})	
				$("#resultEncode").append(finalValue + "<br>")
			}			
		})			
	}
</script>
<textarea rows="2" cols="50" name="toEncode" >
</textarea>
<a id="aEncode" href="javascript:;">Encode</a>
<br>
<div id="resultEncode" style="border:1px"></div>
<br>
