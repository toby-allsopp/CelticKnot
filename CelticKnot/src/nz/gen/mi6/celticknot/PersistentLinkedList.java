package nz.gen.mi6.celticknot;

public class PersistentLinkedList<T> implements PersistentList<T> {

	public PersistentLinkedList(final PersistentLinkedList<T> persistentLinkedList, final T element)
	{
	}

	public PersistentLinkedList()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public PersistentList<T> add(final T element)
	{
		return new PersistentLinkedList<T>(this, element);
	}

}
