package g2pl.basics;

public class Pair<F, S> {
    @SuppressWarnings("unused")
	public F first;
    @SuppressWarnings("unused")
	public S second;
    
    public Pair(F f, S s)
    {
    	first = f;
    	second = s;
    }
}
