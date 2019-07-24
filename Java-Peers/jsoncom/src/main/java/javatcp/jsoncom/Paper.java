package javatcp.jsoncom;

public class Paper {
	private int x;
	private int y;
	
	public Paper() {
		// TODO Auto-generated constructor stub
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Paper(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "Paper [x=" + x + ", y=" + y + "]";
	}
	
}
