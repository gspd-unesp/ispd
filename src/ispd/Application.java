package ispd;

public abstract class Application
{
    final String[] args;

    public Application (String[] args)
    {
        this.args = args;
    }

    public abstract void executar ();
}
