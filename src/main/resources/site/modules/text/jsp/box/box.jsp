<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<webl:property define="alignment">

	<div class="textbox" style="float: <%= alignment %>;">
		<webl:ifelement name="title">
			<h2><webl:element name="title" /></h2>
		</webl:ifelement>
		<p><webl:element name="paragraph" /></p>
	</div>
	<p class="spacer BoxesCol">&nbsp;</p>
	
</webl:property>