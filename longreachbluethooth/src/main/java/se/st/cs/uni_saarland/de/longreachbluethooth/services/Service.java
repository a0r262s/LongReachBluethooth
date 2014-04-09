package se.st.cs.uni_saarland.de.longreachbluethooth.services;

import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;

/** Interface to interact with a given service
 *
 * User: Simon Koch
 * Date: 06.12.13
 * Time: 11:20
 *
 * This Interface represents the only way a non Service related program part should
 * interact with a service.
 */
public interface Service {

    //TODO: this service is expected to do what he is supposed to do - no boolean needed
    /** starts the service
     *
     * starts the service using ip and port to connect to. If ip is null
     * and/or port -1, it will wait for connection from remote peer.
     *
     * @param ip the ip to connect to
     * @param port the port to connect on
     * @return boolean whether the startup was successfull
     */
    public boolean start(String ip,int port) throws ServiceException;

    //TODO: this service is expected to do what he is supposed to do - no boolean needed
    /** stops the service
     *
     * stops the service gently on both sides of the connection
     *
     * @return whether the shut down was successfull
     */
    public boolean stop() throws ServiceException;

    /** port on which the service is running
     *
     * gives the port on which the service is running
     *
     * @return the port on which the service is running
     */
    public int port() throws ServiceException;

    /** sums up the current service status
     *
     * returns a summation of the current status of the service
     *
     * @return  a summation of the current status of the service
     */
    public String toString();

    /** returns the service name
     *
     * returns the service name
     *
     * @return the service name
     */
    public ServiceName getName();


    public boolean running();


}
