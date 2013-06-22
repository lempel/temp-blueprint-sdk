package lempel.blueprint.app.tracer;

//simply serves as a mutable object to be stored in a map
public class ThreadInfo
{
	static final int STACK_SIZE = 512;
	long[] timingStack = new long [STACK_SIZE];
	public int tos = 0; //top of stack
	public int lastPushPos = -1;
	
    //public data member without accessor methods since absolute best performance
    //is critical and outweighs all other factors.
    public int stackSize; 
    
    ThreadInfo(int stackSize)
    {
        this.stackSize = stackSize;
    }
    
    public void pushTiming()
    {
        lastPushPos = tos;
    	if (tos >= STACK_SIZE-1) return;
    	timingStack[tos++] = System.nanoTime();
    }
    
    public long popTiming()
    {    	
    	if (tos<=0) return(-1);
    	return(System.nanoTime()-timingStack[--tos]);
    }
}
