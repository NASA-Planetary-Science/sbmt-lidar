package edu.jhuapl.sbmt.lidar.hyperoctree;

public class HyperException extends Exception
{
	
	public static class HyperDimensionMismatchException extends HyperException
	{
		int dim1,dim2;
		
		public HyperDimensionMismatchException(int dim1, int dim2)
		{
			this.dim1=dim1;
			this.dim2=dim2;
		}
		
		@Override
		public String getMessage()
		{
			return "Hyperdimension mismatch: "+dim1+" "+dim2;
		}
	}
	
	public static class HyperPointOutOfBoundsException extends HyperException
	{
		@Override
		public String getMessage()
		{
			return "HyperPoint out of bounds";
		}
	}

}
