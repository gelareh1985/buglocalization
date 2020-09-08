<?xml version = '1.0' encoding = 'ISO-8859-1' ?>
<asm version="1.0" name="0">
	<cp>
		<constant value="javaChanges2UMLChanges"/>
		<constant value="links"/>
		<constant value="NTransientLinkSet;"/>
		<constant value="col"/>
		<constant value="J"/>
		<constant value="main"/>
		<constant value="A"/>
		<constant value="OclParametrizedType"/>
		<constant value="#native"/>
		<constant value="Collection"/>
		<constant value="J.setName(S):V"/>
		<constant value="OclSimpleType"/>
		<constant value="OclAny"/>
		<constant value="J.setElementType(J):V"/>
		<constant value="TransientLinkSet"/>
		<constant value="A.__matcher__():V"/>
		<constant value="A.__exec__():V"/>
		<constant value="self"/>
		<constant value="__resolve__"/>
		<constant value="1"/>
		<constant value="J.oclIsKindOf(J):B"/>
		<constant value="18"/>
		<constant value="NTransientLinkSet;.getLinkBySourceElement(S):QNTransientLink;"/>
		<constant value="J.oclIsUndefined():B"/>
		<constant value="15"/>
		<constant value="NTransientLink;.getTargetFromSource(J):J"/>
		<constant value="17"/>
		<constant value="30"/>
		<constant value="Sequence"/>
		<constant value="2"/>
		<constant value="A.__resolve__(J):J"/>
		<constant value="QJ.including(J):QJ"/>
		<constant value="QJ.flatten():QJ"/>
		<constant value="e"/>
		<constant value="value"/>
		<constant value="resolveTemp"/>
		<constant value="S"/>
		<constant value="NTransientLink;.getNamedTargetFromSource(JS):J"/>
		<constant value="name"/>
		<constant value="__matcher__"/>
		<constant value="A.__matchJSystemModelRootToUmlSystemModelRoot():V"/>
		<constant value="A.__matchJViewToUmlView():V"/>
		<constant value="A.__matchJChangeToUmlChange():V"/>
		<constant value="__exec__"/>
		<constant value="JSystemModelRootToUmlSystemModelRoot"/>
		<constant value="NTransientLinkSet;.getLinksByRule(S):QNTransientLink;"/>
		<constant value="A.__applyJSystemModelRootToUmlSystemModelRoot(NTransientLink;):V"/>
		<constant value="JViewToUmlView"/>
		<constant value="A.__applyJViewToUmlView(NTransientLink;):V"/>
		<constant value="JChangeToUmlChange"/>
		<constant value="A.__applyJChangeToUmlChange(NTransientLink;):V"/>
		<constant value="__matchJSystemModelRootToUmlSystemModelRoot"/>
		<constant value="SystemModel"/>
		<constant value="JavaSystemModel"/>
		<constant value="IN1"/>
		<constant value="MMOF!Classifier;.allInstancesFrom(S):QJ"/>
		<constant value="TransientLink"/>
		<constant value="NTransientLink;.setRule(MATL!Rule;):V"/>
		<constant value="jSystemModelRoot"/>
		<constant value="NTransientLink;.addSourceElement(SJ):V"/>
		<constant value="umlSystemModelRoot"/>
		<constant value="UMLSystemModel"/>
		<constant value="NTransientLink;.addTargetElement(SJ):V"/>
		<constant value="NTransientLinkSet;.addLink2(NTransientLink;B):V"/>
		<constant value="11:3-15:4"/>
		<constant value="__applyJSystemModelRootToUmlSystemModelRoot"/>
		<constant value="NTransientLink;"/>
		<constant value="NTransientLink;.getSourceElement(S):J"/>
		<constant value="NTransientLink;.getTargetElement(S):J"/>
		<constant value="3"/>
		<constant value="description"/>
		<constant value="views"/>
		<constant value="12:12-12:28"/>
		<constant value="12:12-12:33"/>
		<constant value="12:4-12:33"/>
		<constant value="13:19-13:35"/>
		<constant value="13:19-13:47"/>
		<constant value="13:4-13:47"/>
		<constant value="14:13-14:29"/>
		<constant value="14:13-14:35"/>
		<constant value="14:4-14:35"/>
		<constant value="link"/>
		<constant value="__matchJViewToUmlView"/>
		<constant value="View"/>
		<constant value="jView"/>
		<constant value="umlView"/>
		<constant value="22:3-26:4"/>
		<constant value="__applyJViewToUmlView"/>
		<constant value="model"/>
		<constant value="changes"/>
		<constant value="uml"/>
		<constant value="kind"/>
		<constant value="23:13-23:18"/>
		<constant value="23:13-23:24"/>
		<constant value="23:4-23:24"/>
		<constant value="24:15-24:20"/>
		<constant value="24:15-24:28"/>
		<constant value="24:4-24:28"/>
		<constant value="25:12-25:17"/>
		<constant value="25:4-25:17"/>
		<constant value="__matchJChangeToUmlChange"/>
		<constant value="Change"/>
		<constant value="jChange"/>
		<constant value="umlChange"/>
		<constant value="33:3-37:4"/>
		<constant value="__applyJChangeToUmlChange"/>
		<constant value="type"/>
		<constant value="quantification"/>
		<constant value="location"/>
		<constant value="34:12-34:19"/>
		<constant value="34:12-34:24"/>
		<constant value="34:4-34:24"/>
		<constant value="35:22-35:29"/>
		<constant value="35:22-35:44"/>
		<constant value="35:4-35:44"/>
		<constant value="36:16-36:23"/>
		<constant value="36:16-36:32"/>
		<constant value="36:4-36:32"/>
	</cp>
	<field name="1" type="2"/>
	<field name="3" type="4"/>
	<operation name="5">
		<context type="6"/>
		<parameters>
		</parameters>
		<code>
			<getasm/>
			<push arg="7"/>
			<push arg="8"/>
			<new/>
			<dup/>
			<push arg="9"/>
			<pcall arg="10"/>
			<dup/>
			<push arg="11"/>
			<push arg="8"/>
			<new/>
			<dup/>
			<push arg="12"/>
			<pcall arg="10"/>
			<pcall arg="13"/>
			<set arg="3"/>
			<getasm/>
			<push arg="14"/>
			<push arg="8"/>
			<new/>
			<set arg="1"/>
			<getasm/>
			<pcall arg="15"/>
			<getasm/>
			<pcall arg="16"/>
		</code>
		<linenumbertable>
		</linenumbertable>
		<localvariabletable>
			<lve slot="0" name="17" begin="0" end="24"/>
		</localvariabletable>
	</operation>
	<operation name="18">
		<context type="6"/>
		<parameters>
			<parameter name="19" type="4"/>
		</parameters>
		<code>
			<load arg="19"/>
			<getasm/>
			<get arg="3"/>
			<call arg="20"/>
			<if arg="21"/>
			<getasm/>
			<get arg="1"/>
			<load arg="19"/>
			<call arg="22"/>
			<dup/>
			<call arg="23"/>
			<if arg="24"/>
			<load arg="19"/>
			<call arg="25"/>
			<goto arg="26"/>
			<pop/>
			<load arg="19"/>
			<goto arg="27"/>
			<push arg="28"/>
			<push arg="8"/>
			<new/>
			<load arg="19"/>
			<iterate/>
			<store arg="29"/>
			<getasm/>
			<load arg="29"/>
			<call arg="30"/>
			<call arg="31"/>
			<enditerate/>
			<call arg="32"/>
		</code>
		<linenumbertable>
		</linenumbertable>
		<localvariabletable>
			<lve slot="2" name="33" begin="23" end="27"/>
			<lve slot="0" name="17" begin="0" end="29"/>
			<lve slot="1" name="34" begin="0" end="29"/>
		</localvariabletable>
	</operation>
	<operation name="35">
		<context type="6"/>
		<parameters>
			<parameter name="19" type="4"/>
			<parameter name="29" type="36"/>
		</parameters>
		<code>
			<getasm/>
			<get arg="1"/>
			<load arg="19"/>
			<call arg="22"/>
			<load arg="19"/>
			<load arg="29"/>
			<call arg="37"/>
		</code>
		<linenumbertable>
		</linenumbertable>
		<localvariabletable>
			<lve slot="0" name="17" begin="0" end="6"/>
			<lve slot="1" name="34" begin="0" end="6"/>
			<lve slot="2" name="38" begin="0" end="6"/>
		</localvariabletable>
	</operation>
	<operation name="39">
		<context type="6"/>
		<parameters>
		</parameters>
		<code>
			<getasm/>
			<pcall arg="40"/>
			<getasm/>
			<pcall arg="41"/>
			<getasm/>
			<pcall arg="42"/>
		</code>
		<linenumbertable>
		</linenumbertable>
		<localvariabletable>
			<lve slot="0" name="17" begin="0" end="5"/>
		</localvariabletable>
	</operation>
	<operation name="43">
		<context type="6"/>
		<parameters>
		</parameters>
		<code>
			<getasm/>
			<get arg="1"/>
			<push arg="44"/>
			<call arg="45"/>
			<iterate/>
			<store arg="19"/>
			<getasm/>
			<load arg="19"/>
			<pcall arg="46"/>
			<enditerate/>
			<getasm/>
			<get arg="1"/>
			<push arg="47"/>
			<call arg="45"/>
			<iterate/>
			<store arg="19"/>
			<getasm/>
			<load arg="19"/>
			<pcall arg="48"/>
			<enditerate/>
			<getasm/>
			<get arg="1"/>
			<push arg="49"/>
			<call arg="45"/>
			<iterate/>
			<store arg="19"/>
			<getasm/>
			<load arg="19"/>
			<pcall arg="50"/>
			<enditerate/>
		</code>
		<linenumbertable>
		</linenumbertable>
		<localvariabletable>
			<lve slot="1" name="33" begin="5" end="8"/>
			<lve slot="1" name="33" begin="15" end="18"/>
			<lve slot="1" name="33" begin="25" end="28"/>
			<lve slot="0" name="17" begin="0" end="29"/>
		</localvariabletable>
	</operation>
	<operation name="51">
		<context type="6"/>
		<parameters>
		</parameters>
		<code>
			<push arg="52"/>
			<push arg="53"/>
			<findme/>
			<push arg="54"/>
			<call arg="55"/>
			<iterate/>
			<store arg="19"/>
			<getasm/>
			<get arg="1"/>
			<push arg="56"/>
			<push arg="8"/>
			<new/>
			<dup/>
			<push arg="44"/>
			<pcall arg="57"/>
			<dup/>
			<push arg="58"/>
			<load arg="19"/>
			<pcall arg="59"/>
			<dup/>
			<push arg="60"/>
			<push arg="52"/>
			<push arg="61"/>
			<new/>
			<pcall arg="62"/>
			<pusht/>
			<pcall arg="63"/>
			<enditerate/>
		</code>
		<linenumbertable>
			<lne id="64" begin="19" end="24"/>
		</linenumbertable>
		<localvariabletable>
			<lve slot="1" name="58" begin="6" end="26"/>
			<lve slot="0" name="17" begin="0" end="27"/>
		</localvariabletable>
	</operation>
	<operation name="65">
		<context type="6"/>
		<parameters>
			<parameter name="19" type="66"/>
		</parameters>
		<code>
			<load arg="19"/>
			<push arg="58"/>
			<call arg="67"/>
			<store arg="29"/>
			<load arg="19"/>
			<push arg="60"/>
			<call arg="68"/>
			<store arg="69"/>
			<load arg="69"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="38"/>
			<call arg="30"/>
			<set arg="38"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="70"/>
			<call arg="30"/>
			<set arg="70"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="71"/>
			<call arg="30"/>
			<set arg="71"/>
			<pop/>
		</code>
		<linenumbertable>
			<lne id="72" begin="11" end="11"/>
			<lne id="73" begin="11" end="12"/>
			<lne id="74" begin="9" end="14"/>
			<lne id="75" begin="17" end="17"/>
			<lne id="76" begin="17" end="18"/>
			<lne id="77" begin="15" end="20"/>
			<lne id="78" begin="23" end="23"/>
			<lne id="79" begin="23" end="24"/>
			<lne id="80" begin="21" end="26"/>
			<lne id="64" begin="8" end="27"/>
		</linenumbertable>
		<localvariabletable>
			<lve slot="3" name="60" begin="7" end="27"/>
			<lve slot="2" name="58" begin="3" end="27"/>
			<lve slot="0" name="17" begin="0" end="27"/>
			<lve slot="1" name="81" begin="0" end="27"/>
		</localvariabletable>
	</operation>
	<operation name="82">
		<context type="6"/>
		<parameters>
		</parameters>
		<code>
			<push arg="83"/>
			<push arg="53"/>
			<findme/>
			<push arg="54"/>
			<call arg="55"/>
			<iterate/>
			<store arg="19"/>
			<getasm/>
			<get arg="1"/>
			<push arg="56"/>
			<push arg="8"/>
			<new/>
			<dup/>
			<push arg="47"/>
			<pcall arg="57"/>
			<dup/>
			<push arg="84"/>
			<load arg="19"/>
			<pcall arg="59"/>
			<dup/>
			<push arg="85"/>
			<push arg="83"/>
			<push arg="61"/>
			<new/>
			<pcall arg="62"/>
			<pusht/>
			<pcall arg="63"/>
			<enditerate/>
		</code>
		<linenumbertable>
			<lne id="86" begin="19" end="24"/>
		</linenumbertable>
		<localvariabletable>
			<lve slot="1" name="84" begin="6" end="26"/>
			<lve slot="0" name="17" begin="0" end="27"/>
		</localvariabletable>
	</operation>
	<operation name="87">
		<context type="6"/>
		<parameters>
			<parameter name="19" type="66"/>
		</parameters>
		<code>
			<load arg="19"/>
			<push arg="84"/>
			<call arg="67"/>
			<store arg="29"/>
			<load arg="19"/>
			<push arg="85"/>
			<call arg="68"/>
			<store arg="69"/>
			<load arg="69"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="88"/>
			<call arg="30"/>
			<set arg="88"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="89"/>
			<call arg="30"/>
			<set arg="89"/>
			<dup/>
			<getasm/>
			<push arg="90"/>
			<call arg="30"/>
			<set arg="91"/>
			<pop/>
		</code>
		<linenumbertable>
			<lne id="92" begin="11" end="11"/>
			<lne id="93" begin="11" end="12"/>
			<lne id="94" begin="9" end="14"/>
			<lne id="95" begin="17" end="17"/>
			<lne id="96" begin="17" end="18"/>
			<lne id="97" begin="15" end="20"/>
			<lne id="98" begin="23" end="23"/>
			<lne id="99" begin="21" end="25"/>
			<lne id="86" begin="8" end="26"/>
		</linenumbertable>
		<localvariabletable>
			<lve slot="3" name="85" begin="7" end="26"/>
			<lve slot="2" name="84" begin="3" end="26"/>
			<lve slot="0" name="17" begin="0" end="26"/>
			<lve slot="1" name="81" begin="0" end="26"/>
		</localvariabletable>
	</operation>
	<operation name="100">
		<context type="6"/>
		<parameters>
		</parameters>
		<code>
			<push arg="101"/>
			<push arg="53"/>
			<findme/>
			<push arg="54"/>
			<call arg="55"/>
			<iterate/>
			<store arg="19"/>
			<getasm/>
			<get arg="1"/>
			<push arg="56"/>
			<push arg="8"/>
			<new/>
			<dup/>
			<push arg="49"/>
			<pcall arg="57"/>
			<dup/>
			<push arg="102"/>
			<load arg="19"/>
			<pcall arg="59"/>
			<dup/>
			<push arg="103"/>
			<push arg="101"/>
			<push arg="61"/>
			<new/>
			<pcall arg="62"/>
			<pusht/>
			<pcall arg="63"/>
			<enditerate/>
		</code>
		<linenumbertable>
			<lne id="104" begin="19" end="24"/>
		</linenumbertable>
		<localvariabletable>
			<lve slot="1" name="102" begin="6" end="26"/>
			<lve slot="0" name="17" begin="0" end="27"/>
		</localvariabletable>
	</operation>
	<operation name="105">
		<context type="6"/>
		<parameters>
			<parameter name="19" type="66"/>
		</parameters>
		<code>
			<load arg="19"/>
			<push arg="102"/>
			<call arg="67"/>
			<store arg="29"/>
			<load arg="19"/>
			<push arg="103"/>
			<call arg="68"/>
			<store arg="69"/>
			<load arg="69"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="106"/>
			<call arg="30"/>
			<set arg="106"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="107"/>
			<call arg="30"/>
			<set arg="107"/>
			<dup/>
			<getasm/>
			<load arg="29"/>
			<get arg="108"/>
			<call arg="30"/>
			<set arg="108"/>
			<pop/>
		</code>
		<linenumbertable>
			<lne id="109" begin="11" end="11"/>
			<lne id="110" begin="11" end="12"/>
			<lne id="111" begin="9" end="14"/>
			<lne id="112" begin="17" end="17"/>
			<lne id="113" begin="17" end="18"/>
			<lne id="114" begin="15" end="20"/>
			<lne id="115" begin="23" end="23"/>
			<lne id="116" begin="23" end="24"/>
			<lne id="117" begin="21" end="26"/>
			<lne id="104" begin="8" end="27"/>
		</linenumbertable>
		<localvariabletable>
			<lve slot="3" name="103" begin="7" end="27"/>
			<lve slot="2" name="102" begin="3" end="27"/>
			<lve slot="0" name="17" begin="0" end="27"/>
			<lve slot="1" name="81" begin="0" end="27"/>
		</localvariabletable>
	</operation>
</asm>
