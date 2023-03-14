/**
 * <h2>Requirements for all sub-packages</h2>
 * The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
 * "SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in this
 * document are to be interpreted as described in
 * <a href="https://www.rfc-editor.org/rfc/rfc2119">RFC 2119</a>.
 * 
 * <h2>About serialization</h2>
 * Due to the way the Servlet API works, it is possible for web servers
 * to serialize web sessions, and all objects they reference. For this reason,
 * it is
 * essential to ensure that APIs in this package either support serialization
 * by default or explicitly state that they don't support it.
 * 
 */
package eu.infolead.jtk;
