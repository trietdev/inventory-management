package inventory.dao;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(rollbackFor=Exception.class)
public class BaseDAOImpl<E> implements BaseDAO<E>{
	
	final static Logger log = Logger.getLogger(BaseDAOImpl.class);
	
	@Autowired
	SessionFactory sessionFactory;

	@Override
	public List<E> findAll() {
		log.info("Find all record from database");
		StringBuilder queryString = new StringBuilder();
		queryString.append(" from ").append(getGenericName()).append(" as model where model.activeFlag=1");
		log.info("Query find all ===> " + queryString.toString());
		return sessionFactory.getCurrentSession().createQuery(queryString.toString()).list();
	}

	@Override
	public E findById(Class<E> e, Serializable id) {
		log.info("Find by ID");
		return sessionFactory.getCurrentSession().get(e, id);
	}

	@Override
	public List<E> findByProperty(String property, Object value) {
		System.out.println("===========" + property + "=============");
		log.info("Find by property");
		StringBuilder queryString = new StringBuilder();
		queryString.append(" from ").append(getGenericName()).
			append(" as model where model.activeFlag=1 and model.").append(property).append("=?");
		log.info("Query find by property ===> " + queryString.toString());
		Query<E> query = sessionFactory.getCurrentSession().createQuery(queryString.toString());
		query.setParameter(0, value);
		return query.getResultList();
	}

	@Override
	public void save(E instance) {
		log.info("Save instance");
		sessionFactory.getCurrentSession().persist(instance);
	}

	@Override
	public void update(E instance) {
		log.info("Update instance");
		sessionFactory.getCurrentSession().merge(instance);
	}
	
	public String getGenericName() {
		String s = getClass().getGenericSuperclass().toString();
		Pattern pattern = Pattern.compile("\\<(.*?)\\>");
		Matcher matcher = pattern.matcher(s);
		String generic = "null";
		if(matcher.find()) {
			generic = matcher.group(1);
		}
		return generic;
	}

}
