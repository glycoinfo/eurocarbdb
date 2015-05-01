/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package test.eurocarbdb.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.*;
import org.apache.log4j.Logger;

import org.eurocarbdb.util.Visitor;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.StringUtils.repeat;

/**
*   Tests {@link Visitor}.
*/
@Test( groups="util.visitor" )
public class VisitorTest 
{
    /** When true, runs an extended & benchmarked comparison of visitor speed,
    *   comparing cached reflection-based double-dispatch versus direct dispatch. */
    public static final boolean Run_Time_Trial = true;
    
    /** logging handle */
    static Logger log = Logger.getLogger( VisitorTest.class );

    
    /*~~~~~~  some simple data structures of test classes to visit  ~~~~~~~*/
    
    /** A test data structure */
    private Map<String,Object> m = new HashMap<String,Object>();

       
    private void init()     // populates Map<String,Object> m  
    {
        m.clear();
        m.put("aaaa", 1111 );
        m.put("bbbb", 3333 );
        m.put("cccc", 5555 );
        m.put("dddd", new char[] { 'x', 'y', 'z' } );
        m.put("an object of A", new A() );
        m.put("an object of B", new B() );
    }
    
    /** 
    *   an arbitrary data structure used to compare classic & reflection-based visitors.
    *   order of traversal should be: 
    *<pre>
    *       C, B, C, A, C, A, B, C, A, C, C, B, A, A, C, A
    *</pre>
    *   note here that {@link VD} is a subclass of {@link A}. 
    */
    private final VC data_structure 
        = new VC(                       //  C
            new VB(),                   //  B
            new VC(                     //  C
                new VA(),               //  A
                new VC(                 //  C
                    new VA(),           //  A
                    new VB()            //  B
                ),
                new VC()                //  C
            ),
            new VA(),                   //  A
            new VC(                     //  C
                new VC(                 //  C
                    new VB(),           //  B
                    new VD(),           //  A
                    new VA()            //  A
                )
            ),
            new VC(),                   //  C
            new VD()                    //  A
        );
        
        
    /*~~~~~~~~~~~~~~~~  test methods  ~~~~~~~~~~~~~~~~~~*/
    
    /** Creates a {@link SimpleVisitor} and visits a simple data structure. */
    @Test
    public void visitorSmokeTest()
    {
        init();
        SimpleVisitor s = new SimpleVisitor();
        s.visit( m );
    }
    
    
    /** 
    *   Ensures that dispatching to accept(Xxxx) methods properly respects 
    *   inheritance and interfaces.  
    */
    @Test
    public void visitorDispatch()
    {
        SimpleVisitor s = new SimpleVisitor();

        A a = new A();
        
        //  test basic visitation
        System.out.println("expecting to visit A..."); 
        s.visitedOrder.clear();
        s.visit( a );
        assert s.visitedOrder.equals( Arrays.asList( A.class ) );
        
        //  test regular visitation, while casting to different things 
        B b = new B();
        
        System.out.println("expecting to visit B..."); 
        s.visitedOrder.clear();
        s.visit( b );
        assert s.visitedOrder.equals( Arrays.asList( B.class ) );
        
        System.out.println("expecting to visit B... "); 
        s.visitedOrder.clear();
        s.visit( (A) b );
        assert s.visitedOrder.equals( Arrays.asList( B.class ) );       
        
        System.out.println("expecting to visit B... "); 
        s.visitedOrder.clear();
        s.visit( (Object) b );
        assert s.visitedOrder.equals( Arrays.asList( B.class ) );
        
        //  test regular visitation of a nested data structure
        C c = new C( new A(), new B(), new C() );
        
        System.out.println("expecting to visit C, A, B, C... "); 
        s.visitedOrder.clear();
        s.visit( c );
        assert s.visitedOrder.equals( Arrays.asList( C.class, A.class, B.class, C.class ) );

        
        D d = new D();
        E e = new E();
        
        //  test regular visitation of accept( superclass )
        System.out.println("expecting to visit A... "); 
        s.visitedOrder.clear();
        s.visit( d );
        assert s.visitedOrder.equals( Arrays.asList( A.class ) );
        
        System.out.println("expecting to visit A... "); 
        s.visitedOrder.clear();
        s.visit( (I) d );
        assert s.visitedOrder.equals( Arrays.asList( A.class ) );
        
        //  check visitation when there are no matching accept methods
        System.out.println("expecting to visit nothing... "); 
        s.visitedOrder.clear();
        s.visit( e );
        assert s.visitedOrder.equals( Collections.emptyList() );
        

        s = new InterfaceVisitor();
        
        //  test regular visitation of accept( superclass ) when there 
        //  is also a matching accept( interface ) method
        System.out.println("expecting to visit A... "); 
        s.visit( d );
        assert s.visitedOrder.equals( Arrays.asList( A.class ) );
        
        //  check visitation of accept( interface )
        System.out.println("expecting to visit II... "); 
        s.visitedOrder.clear();
        s.visit( e );
        assert s.visitedOrder.equals( Arrays.asList( II.class ) );
        
        //  test a plain Object
        Object x = new Object();

        System.out.println("expecting to visit Object... "); 
        s.visitedOrder.clear();
        s.visit( x );
        assert s.visitedOrder.equals( Arrays.asList( Object.class ) );
        
        F f = new F();
        System.out.println("expecting to visit Object... "); 
        s.visitedOrder.clear();
        s.visit( f );
        assert s.visitedOrder.equals( Arrays.asList( Object.class ) );
    }

    
    /** 
    *   Compares the reflection-based {@link Visitor} class to an internal
    *   "classic-style" Visitor based on the typical Visitor pattern.
    */
    @Test
    public void visitorComparison()
    {
        //  smoke test the data-structure for both visitors
        System.out.println("The following 2 traversals should look identical:");

        System.out.println();
        System.out.println("1) reflection visitor:");
        SimpleVisitor v1 = new SimpleVisitor(); 
        v1.visit( data_structure );
        
        System.out.println();
        System.out.println("2) traditional visitor:");
        ClassicDoubleDispatchVisitor v2 = new ClassicDoubleDispatchVisitor();
        v2.visit( data_structure );        
        
        //  check visitation order was the same
        System.out.println();
        boolean b = v1.visitedOrder.equals( v2.visitedOrder ); 
        System.out.println("was order of objects traversed the same? " + b );
        if ( ! b )
        {
            System.out.print("reflection visitor (" + v1.visitedOrder.size() + " items): " );
            for ( Object x : v1.visitedOrder )
                System.out.print(" -> " + x );
            System.out.println();
            System.out.print("traditional visitor (" + v2.visitedOrder.size() + " items):" );
            for ( Object x : v2.visitedOrder )
                System.out.print(" -> " + x );
            System.out.println();
        }
        assert b;
        
        if ( ! Run_Time_Trial ) 
            return;
            
        System.out.println();
        System.out.println("comparing traversal speed of classic-v-reflection visitor:");
        System.out.println();

        timeTrial( 1 );
        
        timeTrial( 10 );
        
        timeTrial( 100 );
        
        timeTrial( 1000 );
        
        timeTrial( 10000 );
        
        timeTrial( 100000 );
        
        timeTrial( 500000 );
    }
    
    
    /** 
    *   Compares visit times of a pre-defined {@link data_structure} by a 
    *   {@link SimpleVisitor} and a {@link ClassicDoubleDispatchVisitor}.
    */
    private final void timeTrial( int iterations )
    {
        System.out.println("timing " + iterations + " iteration(s):" );

        SimpleVisitor v1 = new SimpleVisitor() { void print( Object x ) {/* eliminate print time */} };
        long start1 = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ )
        {
            v1.visit( data_structure );
            v1.visitedOrder.clear();
        }
        long end1 = System.currentTimeMillis();
        
        ClassicDoubleDispatchVisitor v2 = new ClassicDoubleDispatchVisitor() { void print( Object x ) {/* eliminate print time */} };
        long start2 = System.currentTimeMillis();
        for ( int i = 0; i < iterations; i++ )
        {
            v2.visit( data_structure );
            v2.visitedOrder.clear();
        }
        long end2 = System.currentTimeMillis();
     
        System.out.println("reflection visitor took " + (end1-start1) );
        System.out.println("traditional visitor took " + (end2-start2) );
        System.out.println();
    }
    

    /*~~~~~~~~~~~~~~~~  some basic test classes  ~~~~~~~~~~~~~~~~~~*/
    
    /** Empty class used for testing purposes only */
    static class A {}
    
    /** Empty class used for testing purposes only */
    static class B extends A {}
    
    /** Empty class used for testing purposes only */
    static class C extends B 
    { 
        public final A[] list; 
    
        public C() { list = null; }
        
        public C( A... a ) { list = a; } 
        
        public String toString() {  return super.toString() + "[" + join(", ", list) + "]";  }
    }
    
    /** Empty class used for testing purposes only */
    static interface I {} 
    
    /** Empty class used for testing purposes only */
    static interface II extends I {} 
    
    /** Empty class used for testing purposes only */
    static class D extends A implements I {}
    
    /** Empty class used for testing purposes only */
    static class E implements II {}
    
    /** Empty class used for testing purposes only */
    static class F {}

    
    /*~~~~~~~~~~~~~~~~  test Visitor classes  ~~~~~~~~~~~~~~~~~~*/
    
    /** Visitor sub-class based on the reflection-based {@link Visitor} class. */
    static class SimpleVisitor extends Visitor
    {
        int indent = -1;
        
        /** records order of visitation */
        public final List<Class> visitedOrder = new ArrayList<Class>();
        
        void before() {  indent++;  }
        
        void after() {  indent--;  }
        
            
        void print( Object x ) 
        {  
            System.out.print( repeat("    ", indent ) ); 
            System.out.print( "> " ); 
            System.out.println( x ); 
        } 
        
        public void accept( String x ) 
        {  
            before();
            visitedOrder.add( String.class );
            print("String '" + x + "' visited");  
            after();
        }   
        
        public void accept( Integer x ) 
        {  
            before();
            visitedOrder.add( Integer.class );
            print("Integer '" + x + "' visited");  
            after();
        }   
        
        public void accept( Map x ) 
        {  
            before();
            visitedOrder.add( Map.class );
            print("Map of " + x.size() + " element(s) visited");  
            for ( Object e : x.entrySet() )
                visit( e );   
            after();
        }   
        
        public void accept( Map.Entry x ) 
        {  
            before();
            visitedOrder.add( Map.Entry.class );
            print("Map.Entry '" + x + "' visited");  
            visit( x.getKey() );
            visit( x.getValue() );
            after();
        }   
        
        public void accept( char[] x )
        {
            before();
            visitedOrder.add( char[].class );
            print("char[] '" + x + "' visited");  
            for ( char c : x )
                visit( c );
            after();            
        }
        
        public void accept( Character x )
        {
            before();
            visitedOrder.add( Character.class );
            print("char '" + x + "' visited");  
            after();            
        }
        
        public void accept( A x )
        {
            before();
            visitedOrder.add( A.class );
            print("Object A visited");  
            after();            
        }

        public void accept( B x )
        {
            before();
            visitedOrder.add( B.class );
            print("Object B visited");  
            after();            
        }
        
        public void accept( C x )
        {
            before();
            visitedOrder.add( C.class );
            print("Object C visited");  
            if ( x.list != null )
                for ( Object a : x.list )
                    visit( a );
            after();            
        }
        
    }
    
    /** Visitor sub-class based on the reflection-based {@link Visitor} class. */
    static class InterfaceVisitor extends SimpleVisitor
    {
        public void accept( Object x )
        {
            before();
            visitedOrder.add( Object.class );
            print("Object '" + x + "' visited");  
            after();
        }

        public void accept( I i )
        {
            before();
            visitedOrder.add( I.class );
            print("Object '" + i + "' visited through interface I");  
            after();
        }

        public void accept( II ii )
        {
            before();
            visitedOrder.add( II.class );
            print("Object '" + ii + "' visited through interface II");
            after();
        }
    }
    
    
    /** Visitor implemented the 'classic' way, using a {@link Visitable} interface */
    static class ClassicDoubleDispatchVisitor 
    {
        int indent = -1;
        
        /** records order of visitation */
        public final List<Class> visitedOrder = new ArrayList<Class>();
        
        void before() {  indent++;  }
        
        void after() {  indent--;  }
        
        void print( Object x ) 
        {  
            System.out.print( repeat("    ", indent ) ); 
            System.out.print( "> " ); 
            System.out.println( x ); 
        } 
        
        public void visit( A x )
        {
            before();
            visitedOrder.add( A.class );
            print("Object A visited");  
            after();            
        }

        public void visit( B x )
        {
            before();
            visitedOrder.add( B.class );
            print("Object B visited");  
            after();            
        }
        
        //  this is only accept method that needs changing since 
        //  have to dispatch differently under the hood.
        public void visit( C x )
        {
            before();
            visitedOrder.add( C.class );
            print("Object C visited");  
            if ( x.list != null )
                for ( Object a : x.list )
                    ((Visitable) a ).accept( this );
            after();            
        }
        
    }
    
    
    /** Indicates the implementing class can be visited by an instance of 
    *   {@link ClassicDoubleDispatchVisitor}. */
    static interface Visitable
    {
        public void accept( ClassicDoubleDispatchVisitor visitor );
    }
    
    
    /** {@link Visitable} version of class {@link A} */
    static class VA extends A implements Visitable 
    {
        public void accept( ClassicDoubleDispatchVisitor visitor )
        {
            visitor.visit( this );   
        }
    }
    
    /** {@link Visitable} version of class {@link B} */
    static class VB extends B implements Visitable 
    {
        public void accept( ClassicDoubleDispatchVisitor visitor )
        {
            visitor.visit( this );   
        }
    }
    
    /** {@link Visitable} version of class {@link C} */
    static class VC extends C implements Visitable 
    {
        public VC() { super(); }
       
        public VC( A... a ) { super( a ); } 
        
        public void accept( ClassicDoubleDispatchVisitor visitor )
        {
            visitor.visit( this );   
        }
    }

    /** {@link Visitable} version of class {@link D} */
    static class VD extends VA {}

    
} // end class








