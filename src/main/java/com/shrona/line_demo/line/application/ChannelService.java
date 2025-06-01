package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.domain.Channel;
import java.util.List;
import java.util.Optional;

public interface ChannelService {

    public List<Channel> findChannelList();

    public Optional<Channel> findChannelById(Long id);

}
