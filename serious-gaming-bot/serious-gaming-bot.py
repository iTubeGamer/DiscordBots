import discord

topic_channels_category_name = "Topic Channels" # name of the category for topic channels
setup_text_channel_name = "setup"  # ID of the setup text-channel
checkmark_emoji = discord.PartialEmoji(name='✅')

class MyClient(discord.Client):

    async def on_ready(self):
        print(f'Logged on as {self.user}!')    

        # read all custom text-channels
        for guild in client.guilds:
            for category in guild.categories:
                if(category.name.upper() == topic_channels_category_name.upper()):
                    for channel in category.text_channels:
                        print(f'Guild "{guild.name}" category "{category.name}" channel "{channel.name}"')


    async def on_raw_reaction_add(self, payload:discord.RawReactionActionEvent):
        # Retrieve topic_channel to subscribe
        topic_channel = await client.getTopicChannel(payload)
        if(topic_channel is None):
            return

        # Add user to topic channel
        
        guild = await self.fetch_guild(payload.guild_id)
        member = await guild.fetch_member(payload.user_id);
        print(f'Adding member {member.name} to topic-channel "{topic_channel.name}" on guild "{guild.name}"')
        await topic_channel.set_permissions(member, view_channel=True)


    async def on_raw_reaction_remove(self, payload:discord.RawReactionActionEvent):
        # Retrieve topic_channel to unsubscribe
        topic_channel = await client.getTopicChannel(payload)
        if(topic_channel is None):
            return

        # Remove user to topic channel
        guild = await self.fetch_guild(payload.guild_id)
        member = await guild.fetch_member(payload.user_id);
        print(f'Removing member {member.name} from topic-channel "{topic_channel.name}" on guild "{guild.name}"')
        await topic_channel.set_permissions(member, view_channel=False) 


    async def getTopicChannel(self, payload:discord.RawReactionActionEvent) -> discord.TextChannel:
        #check that reaction was with checkmark emoji
        if(payload.emoji != checkmark_emoji):
            return

        # check that reaction was in setup channel
        channel = await self.fetch_channel(payload.channel_id)
        if (channel.name.upper() != setup_text_channel_name.upper()):
            return
        
        # Check if we're still in the guild and it's cached
        guild = channel.guild
        if guild is None:
            return        

        # Get name of topic channel
        mesage = message = await channel.fetch_message(payload.message_id)
        name_of_topic_channel = message.content

        # Find topic channel by name
        topic_channel:discord.TextChannel = None
        for category in guild.categories:
            if (category.name.upper() == topic_channels_category_name.upper()):
                for channel in category.text_channels:
                    if (channel.name.upper() == name_of_topic_channel.upper()):
                        topic_channel = channel
        if(topic_channel is None): print(f'Did not find topic channel "{topic_channel.name}" on guild "{guild.name}"')
        return topic_channel


intents = discord.Intents.default()
intents.message_content = True

client = MyClient(intents=intents)
client.run('OTQyMzg0MDUyMjM1ODIxMTg2.GgUEcZ.O5NafhRTf5y6oqO_W8V_7AUM6kB0N3yC_43KlQ')