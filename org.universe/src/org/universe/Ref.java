package org.universe;

import org.universe.jcl.apparency.NotThreadSafe;

import java.io.Serializable;

@NotThreadSafe
public class Ref<T> implements Serializable
{
    T Value;
}
