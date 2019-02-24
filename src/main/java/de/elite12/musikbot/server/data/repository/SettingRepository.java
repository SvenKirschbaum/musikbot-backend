package de.elite12.musikbot.server.data.repository;

import org.springframework.data.repository.CrudRepository;

import de.elite12.musikbot.server.data.entity.Setting;

public interface SettingRepository extends CrudRepository<Setting, String> {
	
}
