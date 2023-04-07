import discord
import config
import asyncio
import logging

topic_channels_category_name = "Topic Channels" # name of the category for topic channels
setup_text_channel_name = "topic-channels"  # name of the setup text-channel
checkmark_emoji = discord.PartialEmoji(name='✅')

#setup logging
handler = logging.FileHandler(filename='/app/log/serious-gaming-bot.log', encoding='utf-8', mode='w')

class MyClient(discord.Client):

    async def on_ready(self):
        logging.info(f'Logged on as {self.user}!')    


    async def on_raw_reaction_add(self, payload:discord.RawReactionActionEvent):
        emoji = payload.emoji
        channel = self.get_channel(payload.channel_id)
        guild = channel.guild
        results = await asyncio.gather(guild.fetch_member(payload.user_id), channel.fetch_message(payload.message_id))
        member = results[0]
        message = results[1]

        # Retrieve topic_channel to subscribe
        topic_channel = await client.getTopicChannel(emoji, channel, message)
        if(topic_channel is None):
            return

        # Add user to topic channel 
        logging.info(f'Adding member {member.name} to topic-channel "{topic_channel.name}" on guild "{guild.name}"')
        await topic_channel.set_permissions(member, view_channel=True)


    async def on_raw_reaction_remove(self, payload:discord.RawReactionActionEvent):
        emoji = payload.emoji
        channel = self.get_channel(payload.channel_id)
        guild = channel.guild
        results = await asyncio.gather(guild.fetch_member(payload.user_id), channel.fetch_message(payload.message_id))
        member = results[0]
        message = results[1]

        # Retrieve topic_channel to unsubscribe
        topic_channel = await client.getTopicChannel(emoji, channel, message)
        if(topic_channel is None):
            return

        # Remove user from topic channel
        logging.info(f'Removing member {member.name} from topic-channel "{topic_channel.name}" on guild "{guild.name}"')
        await topic_channel.set_permissions(member, view_channel=False) 


    async def getTopicChannel(self, emoji, channel, message) -> discord.TextChannel:
        #check that reaction was with checkmark emoji
        if(emoji != checkmark_emoji):
            return

        # check that reaction was in setup channel  
        if (channel.name.upper() != setup_text_channel_name.upper()):
            return    

        # Get name of topic channel
        name_of_topic_channel = message.content

        # Check if guild is still in cache
        guild = channel.guild
        if(guild is None):
            return

        # Find topic channel by name
        topic_channel:discord.TextChannel = None
        for category in guild.categories:
            if (category.name.upper() == topic_channels_category_name.upper()):
                for channel in category.text_channels:
                    if (channel.name.upper() == name_of_topic_channel.upper()):
                        topic_channel = channel
        if(topic_channel is None): logging.error(f'Did not find topic channel "{name_of_topic_channel}" on guild "{guild.name}"')
        return topic_channel


intents = discord.Intents.default()
intents.message_content = True

client = MyClient(intents=intents)
client.run(config.token, log_handler=handler)