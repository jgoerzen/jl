// interface for that need to know *both* about the keyer and the keyer's serial port.
// The port is passed as an Object, which "hides" it from the classloader (in case the 
// javax.comm package isn't available on the runtime system).
package com.loukides.jl.keyer;

public interface KeyerSettable {
  public void setKeyer(Keyer k, Object o);
}