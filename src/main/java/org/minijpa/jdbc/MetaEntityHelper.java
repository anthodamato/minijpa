/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jpa.db.EntityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MetaEntityHelper {

	private static final Logger LOG = LoggerFactory.getLogger(MetaEntityHelper.class);

	public static List<FetchParameter> convertAttributes(List<MetaAttribute> attributes) {
		return attributes.stream().map(a -> FetchParameter.build(a)).collect(Collectors.toList());
	}

	public static FetchParameter toFetchParameter(MetaAttribute attribute) {
		return FetchParameter.build(attribute);
	}

	public static List<FetchParameter> toFetchParameter(List<JoinColumnAttribute> attributes) {
		List<FetchParameter> list = new ArrayList<>();
		for (JoinColumnAttribute a : attributes) {
			FetchParameter columnNameValue = new FetchParameter(a.getColumnName(), a.getType(), a.getDatabaseType(),
					a.getForeignKeyAttribute(), null, true);
			list.add(columnNameValue);
		}

		return list;
	}

	public static List<QueryParameter> convertAbstractAVToQP(
			ModelValueArray<AbstractAttribute> attributeValueArray) throws Exception {
		List<QueryParameter> list = new ArrayList<>();
		for (int i = 0; i < attributeValueArray.size(); ++i) {
			AbstractAttribute a = attributeValueArray.getModel(i);
			QueryParameter queryParameter = new QueryParameter(a.getColumnName(), attributeValueArray.getValue(i),
					a.getType(), a.getSqlType(), a.attributeMapper);
			list.add(queryParameter);
		}

		return list;
	}

	public static List<QueryParameter> convertAVToQP(MetaAttribute a, Object value) throws Exception {
		List<QueryParameter> list = new ArrayList<>();
		if (a.getRelationship() != null) {
			if (a.getRelationship().getJoinColumnMapping().isPresent()) {
				JoinColumnMapping joinColumnMapping = a.getRelationship().getJoinColumnMapping().get();
				Object v = joinColumnMapping.isComposite()
						? joinColumnMapping.getForeignKey().getReadMethod().invoke(value)
						: value;
				LOG.debug("convertAVToQP: v=" + v);
				list.addAll(convertAVToQP(v, a.getRelationship().getJoinColumnMapping().get()));
			}
		} else {
			QueryParameter queryParameter = new QueryParameter(a.getColumnName(), value,
					a.getType(), a.getSqlType(), a.attributeMapper);
			list.add(queryParameter);
		}

		return list;
	}

	public static List<QueryParameter> convertAVToQP(Object value,
			JoinColumnMapping joinColumnMapping) throws Exception {
		List<QueryParameter> list = new ArrayList<>();
		ModelValueArray<JoinColumnAttribute> modelValueArray = new ModelValueArray();
		expand(joinColumnMapping, value, modelValueArray);
		for (int i = 0; i < modelValueArray.size(); ++i) {
			JoinColumnAttribute joinColumnAttribute = modelValueArray.getModel(i);
			MetaAttribute attribute = joinColumnAttribute.getForeignKeyAttribute();
			LOG.debug("convertAVToQP: joinColumnAttribute.getColumnName()=" + joinColumnAttribute.getColumnName());
			QueryParameter queryParameter = new QueryParameter(joinColumnAttribute.getColumnName(), modelValueArray.getValue(i),
					attribute.getType(), attribute.getSqlType(), attribute.attributeMapper);
			list.add(queryParameter);
		}

		return list;
	}

	public static void expand(JoinColumnMapping joinColumnMapping,
			Object value,
			ModelValueArray<JoinColumnAttribute> modelValueArray) throws Exception {
		for (int i = 0; i < joinColumnMapping.size(); ++i) {
			JoinColumnAttribute joinColumnAttribute = joinColumnMapping.get(i);
			MetaAttribute a = joinColumnMapping.get(i).getForeignKeyAttribute();
			LOG.debug("expand: a=" + a);
			LOG.debug("expand: a.getReadMethod()=" + a.getReadMethod());
			LOG.debug("expand: value=" + value);
			Object v = a.getReadMethod().invoke(value);
			modelValueArray.add(joinColumnAttribute, v);
		}
	}

	public static List<QueryParameter> convertAVToQP(Pk pk, Object value) throws Exception {
		LOG.debug("convertAVToQP: pk=" + pk + "; value=" + value);
		List<QueryParameter> list = new ArrayList<>();
		if (pk.isEmbedded()) {
			ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray();
			expand(pk, value, modelValueArray);
			list.addAll(convertAVToQP(modelValueArray));
			return list;
		}

		QueryParameter queryParameter = new QueryParameter(pk.getAttribute().getColumnName(), value,
				pk.getType(), pk.getAttribute().getSqlType(), pk.getAttribute().attributeMapper);
		list.add(queryParameter);
		return list;
	}

	public static List<QueryParameter> convertAVToQP(ModelValueArray<MetaAttribute> modelValueArray) throws Exception {
		List<QueryParameter> list = new ArrayList<>();
		for (int i = 0; i < modelValueArray.size(); ++i) {
			LOG.debug("convertAVToQP: modelValueArray.getModel(i)=" + modelValueArray.getModel(i));
			list.addAll(convertAVToQP(modelValueArray.getModel(i), modelValueArray.getValue(i)));
		}

		return list;
	}

	public static List<QueryParameter> createJoinColumnAVSToQP(List<JoinColumnAttribute> joinColumnAttributes,
			Pk owningId, Object joinTableForeignKey) throws Exception {
		ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray<>();
		expand(owningId, joinTableForeignKey, modelValueArray);
		List<QueryParameter> queryParameters = new ArrayList<>();
		for (int i = 0; i < modelValueArray.size(); ++i) {
			MetaAttribute attribute = modelValueArray.getModel(i);
			int index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, attribute);
			MetaAttribute metaAttribute = joinColumnAttributes.get(index).getForeignKeyAttribute();
			QueryParameter qp = new QueryParameter(joinColumnAttributes.get(index).getColumnName(), modelValueArray.getValue(i),
					metaAttribute.getType(), metaAttribute.getSqlType(), metaAttribute.attributeMapper);
			queryParameters.add(qp);
		}

		return queryParameters;
	}

	public static List<FetchParameter> convertAllAttributes(MetaEntity entity) {
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<FetchParameter> fetchColumnNameValues = convertAttributes(expandedAttributes);
		fetchColumnNameValues.addAll(toFetchParameter(entity.expandJoinColumnAttributes()));
		return fetchColumnNameValues;
	}

	public static TableColumn toValue(MetaAttribute attribute, FromTable fromTable) {
		return new TableColumn(fromTable, new Column(attribute.columnName));
	}

	public static List<Value> toValues(MetaEntity entity, FromTable fromTable) {
		List<Value> values = new ArrayList<>();
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		for (MetaAttribute attribute : expandedAttributes) {
			TableColumn tableColumn = new TableColumn(fromTable, new Column(attribute.columnName));
			values.add(tableColumn);
		}

		for (JoinColumnAttribute joinColumnAttribute : entity.expandJoinColumnAttributes()) {
			TableColumn tableColumn = new TableColumn(fromTable, new Column(joinColumnAttribute.columnName));
			values.add(tableColumn);
		}

		return values;
	}

	public static List<TableColumn> toValues(List<MetaAttribute> attributes, FromTable fromTable) {
		List<TableColumn> tableColumns = new ArrayList<>();
		for (MetaAttribute metaAttribute : attributes) {
			TableColumn tableColumn = new TableColumn(fromTable, new Column(metaAttribute.getColumnName()));
			tableColumns.add(tableColumn);
		}

		return tableColumns;
	}

	public static List<TableColumn> queryParametersToTableColumns(List<QueryParameter> parameters, FromTable fromTable) {
		return parameters.stream().map(p -> new TableColumn(fromTable, new Column(p.getColumnName())))
				.collect(Collectors.toList());
	}

	public static List<TableColumn> attributesToTableColumns(List<AbstractAttribute> attributes, FromTable fromTable) {
		return attributes.stream().map(a -> new TableColumn(fromTable, new Column(a.getColumnName())))
				.collect(Collectors.toList());
	}

	public static boolean hasOptimisticLock(MetaEntity entity, Object entityInstance)
			throws IllegalAccessException, InvocationTargetException {
		LockType lockType = (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
		if (lockType == LockType.OPTIMISTIC || lockType == LockType.OPTIMISTIC_FORCE_INCREMENT)
			return true;

		return entity.getVersionAttribute().isPresent();
	}

	public static void updateVersionAttributeValue(MetaEntity entity, Object entityInstance) throws Exception {
		if (!hasOptimisticLock(entity, entityInstance))
			return;

		Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
		Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
		entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, versionValue);
	}

	public static void createVersionAttributeArrayEntry(MetaEntity entity, Object entityInstance,
			ModelValueArray<MetaAttribute> modelValueArray) throws Exception {
		if (!hasOptimisticLock(entity, entityInstance))
			return;

		Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
		Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
		modelValueArray.add(entity.getVersionAttribute().get(), versionValue);
	}

	public static void expand(Pk pk,
			Object value,
			ModelValueArray<MetaAttribute> modelValueArray) throws Exception {
		if (pk.isEmbedded()) {
			for (MetaAttribute a : pk.getAttributes()) {
				LOG.debug("expand: a=" + a);
				LOG.debug("expand: a.getReadMethod()=" + a.getReadMethod());
				LOG.debug("expand: value=" + value);
				Object v = a.getReadMethod().invoke(value);
				modelValueArray.add(a, v);
			}
		} else
			modelValueArray.add(pk.getAttribute(), value);
	}

	public static LockType getLockType(MetaEntity entity, Object entityInstance) throws Exception {
		return (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
	}

	public static void setLockType(MetaEntity entity, Object entityInstance, LockType lockType) throws Exception {
		entity.getLockTypeAttributeWriteMethod().get().invoke(entityInstance, lockType);
	}

	public static Optional<QueryParameter> generateVersionParameter(MetaEntity metaEntity) throws Exception {
		if (!metaEntity.hasVersionAttribute())
			return Optional.empty();

		Object value = null;
		MetaAttribute attribute = metaEntity.getVersionAttribute().get();
		Class<?> type = attribute.getType();
		if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
			value = 0;
		} else if (type == Short.class || (type.isPrimitive() && type.getName().equals("short"))) {
			value = Short.valueOf("0");
		} else if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
			value = 0L;
		} else if (type == Timestamp.class) {
			value = Timestamp.from(Instant.now());
		}

		List<QueryParameter> parameters = convertAVToQP(metaEntity.getVersionAttribute().get(), value);
		return Optional.of(parameters.get(0));
	}

	public static EntityStatus getEntityStatus(MetaEntity entity, Object entityInstance) throws Exception {
		return (EntityStatus) entity.getEntityStatusAttributeReadMethod().get().invoke(entityInstance);
	}

	public static void setEntityStatus(MetaEntity entity, Object entityInstance, EntityStatus entityStatus) throws Exception {
		entity.getEntityStatusAttributeWriteMethod().get().invoke(entityInstance, entityStatus);
	}

	public static void setForeignKeyValue(MetaAttribute attribute, Object entityInstance, Object value)
			throws IllegalAccessException, InvocationTargetException {
		attribute.getJoinColumnWriteMethod().get().invoke(entityInstance, value);
	}

	public static Object getForeignKeyValue(MetaAttribute attribute, Object entityInstance)
			throws IllegalAccessException, InvocationTargetException {
		return attribute.getJoinColumnReadMethod().get().invoke(entityInstance);
	}

	public static boolean isFlushed(MetaEntity entity, Object entityInstance) throws Exception {
		EntityStatus entityStatus = getEntityStatus(entity, entityInstance);
		return entityStatus == EntityStatus.FLUSHED || entityStatus == EntityStatus.FLUSHED_LOADED_FROM_DB;
	}

	public static boolean isDetached(MetaEntity entity, Object entityInstance) throws Exception {
		EntityStatus entityStatus = getEntityStatus(entity, entityInstance);
		return entityStatus == EntityStatus.DETACHED;
	}

	public static void removeModificationAttribute(MetaEntity entity, Object parent, String attributeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = entity.getModificationAttributeReadMethod();
		List list = (List) m.invoke(parent);
		list.remove(attributeName);
	}

	private static void clearModificationAttributes(MetaEntity entity, Object parent)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = entity.getModificationAttributeReadMethod();
		List list = (List) m.invoke(parent);
		list.clear();
	}

	public static void removeChanges(MetaEntity entity, Object entityInstance) throws IllegalAccessException, InvocationTargetException {
		MetaEntityHelper.clearModificationAttributes(entity, entityInstance);
	}

	public static ModelValueArray<MetaAttribute> getModifications(MetaEntity entity, Object entityInstance)
			throws IllegalAccessException, InvocationTargetException {
		ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray();
		Method m = entity.getModificationAttributeReadMethod();
		List list = (List) m.invoke(entityInstance);
		if (list.isEmpty())
			return modelValueArray;

		for (Object p : list) {
			String v = (String) p;
			MetaAttribute attribute = entity.getAttribute(v);
			if (attribute == null) {
				Optional<MetaEntity> embeddable = entity.getEmbeddable(v);
				if (embeddable.isPresent()) {
					Object embeddedInstance = MetaEntityHelper.getEmbeddableValue(entityInstance, embeddable.get());
					ModelValueArray<MetaAttribute> mva = getModifications(embeddable.get(), embeddedInstance);
					modelValueArray.add(mva);
				}
			} else {
				Object value = attribute.getReadMethod().invoke(entityInstance);
				modelValueArray.add(attribute, value);
			}
		}

		return modelValueArray;
	}

	public static List getJoinColumnPostponedUpdateAttributeList(MetaEntity entity, Object parentInstance)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = entity.getJoinColumnPostponedUpdateAttributeReadMethod().get();
		return (List) m.invoke(parentInstance);
	}

	public static Object getAttributeValue(Object parentInstance, MetaAttribute attribute) throws Exception {
		return attribute.getReadMethod().invoke(parentInstance);
	}

	public static Object getEmbeddableValue(Object parentInstance, MetaEntity embeddable) throws IllegalAccessException, InvocationTargetException {
		return embeddable.getReadMethod().invoke(parentInstance);
	}

	public static Object writeMetaAttributeValue(Object parentInstance, Class<?> parentClass, MetaAttribute attribute,
			Object value, MetaEntity entity) throws Exception {
		Object parent = parentInstance;
		if (parent == null)
			parent = parentClass.getDeclaredConstructor().newInstance();

		LOG.debug("writeMetaAttributeValue: parent=" + parent + "; a.getWriteMethod()=" + attribute.getWriteMethod());
		LOG.debug("writeMetaAttributeValue: value=" + value);
		if (value != null)
			LOG.debug("writeMetaAttributeValue: value.getClass()=" + value.getClass());

		attribute.getWriteMethod().invoke(parent, value);
		MetaEntityHelper.removeModificationAttribute(entity, parent, attribute.getName());
		return parent;
	}

	public static Object writeEmbeddableValue(Object parentInstance, Class<?> parentClass, MetaEntity embeddable,
			Object value, MetaEntity entity) throws Exception {
		Object parent = parentInstance;
		if (parent == null) {
			parent = parentClass.getDeclaredConstructor().newInstance();
		}

		LOG.debug("writeEmbeddableValue: parent=" + parent + "; a.getWriteMethod()=" + embeddable.getWriteMethod());
		LOG.debug("writeEmbeddableValue: value=" + value);

		embeddable.getWriteMethod().invoke(parent, value);
		MetaEntityHelper.removeModificationAttribute(entity, parent, embeddable.getName());
		return parent;
	}

	public static Object writeAttributeValue(MetaEntity entity, Object parentInstance, MetaAttribute attribute,
			Object value) throws Exception {
		LOG.debug("writeAttributeValue: parentInstance=" + parentInstance);
		LOG.debug("writeAttributeValue: attribute.getName()=" + attribute.getName() + "; value=" + value);
		return findAndSetAttributeValue(entity.getEntityClass(), parentInstance, attribute,
				value, entity);
	}

	private static Object findAndSetAttributeValue(Class<?> parentClass, Object parentInstance,
			MetaAttribute attribute, Object value, MetaEntity entity)
			throws Exception {
		LOG.debug("findAndSetAttributeValue: value=" + value + "; attribute=" + attribute);
		LOG.debug("findAndSetAttributeValue: parentInstance=" + parentInstance + "; parentClass=" + parentClass);
		LOG.debug("findAndSetAttributeValue: entity=" + entity);

		// search over all attributes
		for (MetaAttribute a : entity.getAttributes()) {
			if (a == attribute) {
				return MetaEntityHelper.writeMetaAttributeValue(parentInstance, parentClass, a, value, entity);
			}
		}

		for (MetaEntity embeddable : entity.getEmbeddables()) {
			Object parent = MetaEntityHelper.getEmbeddableValue(parentInstance, embeddable);
			Object aInstance = findAndSetAttributeValue(embeddable.getEntityClass(), parent,
					attribute, value, embeddable);
			if (aInstance != null) {
				return MetaEntityHelper.writeEmbeddableValue(parentInstance, parentClass, embeddable, aInstance, entity);
			}
		}

		return null;
	}

	public static Object build(MetaEntity entity, Object idValue) throws Exception {
		Object entityInstance = entity.getEntityClass().getDeclaredConstructor().newInstance();
		LOG.debug("build: entityInstance=" + entityInstance);
		LOG.debug("build: idValue=" + idValue);
		LOG.debug("build: idValue.getClass()=" + idValue.getClass());
		LOG.debug("build: entity.getId().getWriteMethod()=" + entity.getId().getWriteMethod());
		entity.getId().getWriteMethod().invoke(entityInstance, idValue);
		return entityInstance;
	}

	public static boolean isLazyAttributeLoaded(MetaEntity entity, MetaAttribute a, Object entityInstance)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = entity.getLazyLoadedAttributeReadMethod().get();
		List list = (List) m.invoke(entityInstance);
		return list.contains(a.getName());
	}

	public static void lazyAttributeLoaded(MetaEntity entity, MetaAttribute a, Object entityInstance, boolean loaded)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = entity.getLazyLoadedAttributeReadMethod().get();
		List list = (List) m.invoke(entityInstance);
		if (loaded) {
			if (!list.contains(a.getName()))
				list.add(a.getName());
		} else {
			list.remove(a.getName());
		}
	}

	public static void clearLazyAttributeLoaded(MetaEntity entity, Object entityInstance)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (entity.getLazyLoadedAttributeReadMethod().isEmpty())
			return;

		Method m = entity.getLazyLoadedAttributeReadMethod().get();
		List list = (List) m.invoke(entityInstance);
		list.clear();
	}

}
